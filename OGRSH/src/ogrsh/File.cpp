#include <errno.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <dirent.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/FileDescriptorTable.hpp"
#include "ogrsh/FileStream.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/ProtectedTable.hpp"

#include "ogrsh/shims/File.hpp"

using namespace ogrsh;

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(int, creat, (const char *pathname, mode_t mode),
			(pathname, mode))
		{
			return open(pathname, O_CREAT | O_WRONLY | O_TRUNC, mode);
		}

		SHIM_DEF(int, creat64, (const char *pathname, mode_t mode),
			(pathname, mode))
		{
			return open64(pathname, O_CREAT | O_WRONLY | O_TRUNC, mode);
		}

		SHIM_DEF(int, openat,
			(int fd, const char *pathname, int flags, mode_t mode),
			(fd, pathname, flags, mode))
		{
			return openat64(fd, pathname, flags, mode);
		}

		SHIM_DEF(int, openat64,
			(int fd, const char *pathname, int flags, mode_t mode),
			(fd, pathname, flags, mode))
		{
			OGRSH_TRACE("openat64(" << fd << ", \"" << pathname
				<< "\", " << flags << ", ...) called.");

			if ((pathname[0] == '/') || (fd == AT_FDCWD))
			{
				return open64(pathname, flags, mode);
			} else
			{
				FileDescriptor *desc =
					FileDescriptorTable::getInstance().lookup(fd);
				if (desc == NULL)
				{
					// It's not a file descrpitor of ours, so we pass
					// it through
					return ogrsh::shims::real_openat64(fd,
						pathname, flags, mode);
				} else
				{
					// It's one of ours, so we'll have to deal with it
					std::string fullVirtualPath =
						desc->getFullVirtualPath();
					if (fullVirtualPath.length() == 0)
					{
						OGRSH_FATAL("openat64 received a file descriptor "
							<< "with no path set.  Can't open...");
						ogrsh::shims::real_exit(1);
					}

					Path p = Path::getCurrentWorkingDirectory();
					p = p.lookup(fullVirtualPath + "/" + pathname);
					std::string newPath = (const std::string&)p;
					OGRSH_DEBUG("Converted openat64 path to \""
						<< newPath.c_str() << "\".");
					return open64(newPath.c_str(), flags, mode);
				}
			}

			return -1;
		}

		SHIM_DEF(int, open, (const char *path, int flags, mode_t mode),
			(path, flags, mode))
		{
			return open64(path, flags, mode);
		}

		SHIM_DEF(int, open64, (const char *path, int flags, mode_t mode),
			(path, flags, mode))
		{
			// We have to worry about execve weirdness here (see execve)
			ExecuteState state = ExecuteState::fromEnvironment();
			const char *virt = state.getVirtualPath();
			const char *real = state.getRealPath();
			if ( (virt != NULL) && (real != NULL) && (strcmp(real, path) == 0))
			{
				OGRSH_TRACE("Open64 saw an execute state of virtual["
					<< virt << "] and real[" << real << "].");
				path = virt;
			}

			OGRSH_TRACE("open64(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			FileDescriptor* desc = rootMount->getFileFunctions()->open64(
				fullPath, flags, mode);
			if (desc == NULL)
				return -1;

			int ret = FileDescriptorTable::getInstance().insert(desc);
			if (ret < 0)
				delete desc;

			return ret;
		}

		SHIM_DEF(int, close, (int fd), (fd))
		{
			OGRSH_DEBUG("close(" << fd << ") called.");
			if (fd < 0)
				return 0;

			if (ProtectedTable::getInstance().isProtected(fd))
			{
				OGRSH_DEBUG("Attempt made to close a protected file "
					<< "descriptor...ignoring.");
				return 0;
			}

			int ret = FileDescriptorTable::getInstance().close(fd);
			if (ret < 0 && errno == EBADF)
				return ogrsh::shims::real_close(fd);

			return ret;
		}

		SHIM_DEF(int, unlink, (const char *path), (path))
		{
			OGRSH_TRACE("unlink(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getFileFunctions()->unlink(fullPath);
		}

		SHIM_DEF(int, unlinkat, (int dirfd, const char *path, int flags),
			(dirfd, path, flags))
		{
			OGRSH_TRACE("unlinkat(" << dirfd << ", \"" << path << "\", "
				<< flags << ") called.");

			if ((path[0] == '/') || (dirfd == AT_FDCWD))
			{
				Path fullPath =
					Path::getCurrentWorkingDirectory().lookup(path);
				Mount *rootMount =
					Configuration::getConfiguration().getRootMount();

				if (flags & AT_REMOVEDIR)
					return rootMount->getDirectoryFunctions()->rmdir(fullPath);
				else
					return rootMount->getFileFunctions()->unlink(fullPath);
			} else
			{
				FileDescriptor *desc =
					FileDescriptorTable::getInstance().lookup(dirfd);
				if (desc == NULL)
				{
					// It's not a file descrpitor of ours, so we pass
					// it through
					return ogrsh::shims::real_unlinkat(dirfd, path, flags);
				} else
				{
					// It's one of ours, so we'll have to deal with it
					std::string fullVirtualPath =
						desc->getFullVirtualPath();
					if (fullVirtualPath.length() == 0)
					{
						OGRSH_FATAL("unlinkat received a file descriptor "
							<< "with no path set.  Can't unlink...");
						ogrsh::shims::real_exit(1);
					}

					Path p = Path::getCurrentWorkingDirectory();
					p = p.lookup(fullVirtualPath + "/" + path);
					std::string newPath = (const std::string&)p;
					OGRSH_DEBUG("Converted unlinkat path to \""
						<< newPath.c_str() << "\".");
					return unlinkat(AT_FDCWD, newPath.c_str(), flags);
				}
			}

		}

		SHIM_DEF(ssize_t, read, (int fd, void *buf, size_t count),
			(fd, buf, count))
		{
			OGRSH_TRACE("read(" << fd << ", ..., " << count << ") called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real_read(fd, buf, count);
			}

			return desc->read(buf, count);
		}

		SHIM_DEF(ssize_t, write, (int fd, const void *buf, size_t count),
			(fd, buf, count))
		{
			OGRSH_TRACE("write(" << fd << ", ..., " << count << ") called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real_write(fd, buf, count);
			}

			return desc->write(buf, count);
		}

		SHIM_DEF(off_t, lseek, (int fd, off_t offset, int whence),
			(fd, offset, whence))
		{
			OGRSH_TRACE("lseek(" << fd << ", " << offset << ", "
				<< whence << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_lseek(fd, offset, whence);
			}

			return desc->lseek64(offset, whence);
		}

		SHIM_DEF(off64_t, lseek64, (int fd, off64_t offset, int whence),
			(fd, offset, whence))
		{
			OGRSH_TRACE("lseek64(" << fd << ", " << offset << ", "
				<< whence << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_lseek64(fd, offset, whence);
			}

			return desc->lseek64(offset, whence);
		}

		SHIM_DEF(int, _llseek, (unsigned int fd, unsigned long offsethigh,
			unsigned long offsetlow, loff_t *result, unsigned int whence),
			(fd, offsethigh, offsetlow, result, whence))
		{
			off64_t offset;
			off64_t res;

			OGRSH_TRACE("_llseek(" << fd << ", " << offsethigh << ", "
				<< offsetlow << ", ..., " << whence << ") called.");

			offset = offsethigh;
			offset <<= 32;
			offset |= offsetlow;
			res = lseek64((int)fd, offset, (int)whence);
			if (res >= 0)
			{
				*result = res;
				return 0;
			}

			return -1;
		}

		SHIM_DEF(int, fcntl, (int fd, int cmd, long arg), (fd, cmd, arg))
		{
			OGRSH_TRACE("fcntl(" << fd << ", " << cmd << ", ...) called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fcntl(fd, cmd, arg);
			}

			return desc->fcntl(cmd, arg);
		}

		SHIM_DEF(int, fsync, (int fd), (fd))
		{
			OGRSH_TRACE("fsync(" << fd << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fsync(fd);
			}

			return desc->fsync();
		}

		SHIM_DEF(int, setvbuf, (FILE *stream, char *buf, int mode, size_t size),
			(stream, buf, mode, size))
		{
			OGRSH_TRACE("setvbuf(..., mode=" << mode << ", size = "
				<< size << ") called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					OGRSH_FATAL( "setvbuf on intercepted stream not "
						<< "supported in OGRSH.");
					ogrsh::shims::real_exit(1);
				} else
				{
					return real_setvbuf(stream, buf, mode, size);
				}
			} else
			{
				errno = EBADF;
			}

			return -1;
		}

		SHIM_DEF(void, clearerr, (FILE *stream), (stream))
		{
			OGRSH_TRACE("clearerr(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					OGRSH_FATAL( "clearerr on intercepted stream not "
						<< "supported in OGRSH.");
					ogrsh::shims::real_exit(1);
				} else
				{
					real_clearerr(stream);
				}
			} else
			{
				errno = EBADF;
			}
		}

		SHIM_DEF(FILE*, fopen, (const char *path, const char *mode),
			(path, mode))
		{
			OGRSH_TRACE("fopen(\"" << path << "\", \"" << mode
				<< "\") called.");

			FileStream *ret = new FileStream(path, mode);
			if (ret != NULL && (ret->fileno() < 0))
			{
				delete ret;
				ret = NULL;
			}

			return (FILE*)ret;
		}

		SHIM_DEF(FILE*, fopen64, (const char *path, const char *mode),
			(path, mode))
		{
			OGRSH_TRACE("fopen64(\"" << path << "\", \"" << mode
				<< "\") called.");

			FileStream *ret = new FileStream(path, mode);
			if (ret != NULL && (ret->fileno() < 0))
			{
				delete ret;
				ret = NULL;
			}

			return (FILE*)ret;
		}

		SHIM_DEF(FILE*, fdopen, (int fd, const char *mode),
			(fd, mode))
		{
			OGRSH_TRACE("fdopen(" << fd << ", \"" << mode
				<< "\") called.");

			if (fd < 0)
				return NULL;

			FileStream *ret = new FileStream(fd);
			return (FILE*)ret;
		}

		SHIM_DEF(int, fflush, (FILE *fptr), (fptr))
		{
			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fflush();
					return 0;
				} else
				{
					return real_fflush(fptr);
				}
			} else
			{
				errno = EBADF;
				return EOF;
			}
		}

		SHIM_DEF(int, fflush_unlocked, (FILE *fptr), (fptr))
		{
			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fflush();
					return 0;
				} else
				{
					return real_fflush_unlocked(fptr);
				}
			} else
			{
				errno = EBADF;
				return EOF;
			}
		}

		SHIM_DEF(long, ftell, (FILE *fptr), (fptr))
		{
			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->ftell();
				} else
				{
					return real_ftell(fptr);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(off_t, ftello, (FILE *fptr), (fptr))
		{
			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->ftell();
				} else
				{
					return real_ftell(fptr);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(int, fseek, (FILE *fptr, long offset, int whence),
			(fptr, offset, whence))
		{
			OGRSH_TRACE("fseek(..., " << offset << ", " << whence
				<< ") called.");

			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fseek(offset, whence);
					return 0;
				} else
				{
					return real_fseek(fptr, offset, whence);
				}
			} else
			{
				errno = EBADF;
				return EOF;
			}
		}

		SHIM_DEF(int, fseeko, (FILE *fptr, off_t offset, int whence),
			(fptr, offset, whence))
		{
			if (fptr != NULL)
			{
				FileStream *fStream = (FileStream*)fptr;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fseek(offset, whence);
					return 0;
				} else
				{
					return real_fseek(fptr, offset, whence);
				}
			} else
			{
				errno = EBADF;
				return EOF;
			}
		}

		SHIM_DEF(int, fclose, (FILE *stream), (stream))
		{
			OGRSH_TRACE("fclose(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					delete fStream;
					return 0;
				} else
				{
					return real_fclose(stream);
				}
			} else
			{
				errno = EBADF;
				return EOF;
			}
		}

		SHIM_DEF(char*, fgets, (char *s, int n, FILE *stream),
			(s, n, stream))
		{
			OGRSH_TRACE("fgets(..., " << n << ", ...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fgets(s, n);
				} else
				{
					return real_fgets(s, n, stream);
				}
			} else
			{
				errno = EBADF;
				return NULL;
			}
		}

		SHIM_DEF(int, fputs, (const char *s, FILE *stream), (s, stream))
		{
			OGRSH_TRACE("fputs(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fwrite(s, 1, strlen(s));
				} else
				{
					return real_fputs(s, stream);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(char*, fgets_unlocked, (char *s, int n, FILE *stream),
			(s, n, stream))
		{
			OGRSH_TRACE("fgets_unlocked(..., " << n << ", ...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fgets(s, n);
				} else
				{
					return real_fgets_unlocked(s, n, stream);
				}
			} else
			{
				errno = EBADF;
				return NULL;
			}
		}

		SHIM_DEF(size_t, fread, (void *ptr, size_t size, size_t nmemb,
			FILE *stream), (ptr, size, nmemb, stream))
		{
			OGRSH_TRACE("fread(..., " << size << ", " << nmemb
				<< ", ...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fread(ptr, size, nmemb);
				} else
				{
					return real_fread(ptr, size, nmemb, stream);
				}
			} else
			{
				errno = EBADF;
				return 0;
			}
		}

		SHIM_DEF(size_t, fwrite, (const void *ptr, size_t size, size_t nmemb,
			FILE *stream), (ptr, size, nmemb, stream))
		{
			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fwrite(ptr, size, nmemb);
				} else
				{
					return real_fwrite(ptr, size, nmemb, stream);
				}
			} else
			{
				errno = EBADF;
				return 0;
			}
		}

		SHIM_DEF(int, fputc, (int c, FILE *stream), (c, stream))
		{
			char cc = (char)c;
			return (int)fwrite(&cc, 1, 1, stream);
		}

		SHIM_DEF(int, putc, (int c, FILE *stream), (c, stream))
		{
			return fputc(c, stream);
		}

		SHIM_DEF(int, _IO_putc, (int c, FILE *stream), (c, stream))
		{
			return fputc(c, stream);
		}

		SHIM_DEF(int, fgetc, (FILE *stream), (stream))
		{
			OGRSH_TRACE("fgetc(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fgetc();
				} else
				{
					return real_fgetc(stream);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(int, getc, (FILE *stream), (stream))
		{
			OGRSH_TRACE("getc(...) called.");
			return fgetc(stream);
		}

		SHIM_DEF(int, _IO_getc, (FILE *stream), (stream))
		{
			OGRSH_TRACE("_IO_getc(...) called.");
			return fgetc(stream);
		}

		SHIM_DEF(int, feof, (FILE *stream), (stream))
		{
			OGRSH_TRACE("feof(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->feof();
				} else
				{
					return real_feof(stream);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(int, _IO_feof, (FILE *stream), (stream))
		{
			OGRSH_TRACE("_IO_feof(...) called.");
			return feof(stream);
		}

		SHIM_DEF(int, ferror, (FILE *stream), (stream))
		{
			OGRSH_TRACE("ferror(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->ferror();
				} else
				{
					return real_ferror(stream);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(int, _IO_ferror, (FILE *stream), (stream))
		{
			OGRSH_TRACE("_IO_ferror(...) called.");
			return ferror(stream);
		}

		SHIM_DEF(int, fileno, (FILE *stream), (stream))
		{
			OGRSH_TRACE("fileno(...) called.");

			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fileno();
				} else
				{
					return real_fileno(stream);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}

		SHIM_DEF(int, vfprintf, (FILE *stream, const char *format, va_list ap),
			(stream, format, ap))
		{
			if (stream != NULL)
			{
				FileStream *fStream = (FileStream*)stream;

				if (fStream->_magicNumber == FILE_STREAM_MAGIC_NUMBER)
				{
					return fStream->fprintf(format, ap);
				} else
				{
					return real_vfprintf(stream, format, ap);
				}
			} else
			{
				errno = EBADF;
				return -1;
			}
		}
/* MOOCH
#include "File.fprintf.inc"
*/

extern "C" {
	int fprintf(FILE *stream, const char *format, ...)
	{
		int result;
		va_list ap;

		va_start(ap, format);
		result = vfprintf(stream, format, ap);
		va_end(ap);

		return result;
	}

	int __fprintf_chk(FILE *stream, int flag, const char *format, ...)
	{
		int result;
		va_list ap;

		va_start(ap, format);
		result = vfprintf(stream, format, ap);
		va_end(ap);

		return result;
	}
}

		int uber_real_fprintf(FILE *file, const char *format, ...)
		{
			int result;

			va_list ap;
			va_start(ap, format);
			result = real_vfprintf(file, format, ap);
			va_end(ap);

			return result;
		}

		void startFileShims()
		{
			START_SHIM(openat);
			START_SHIM(openat64);
			START_SHIM(open);
			START_SHIM(open64);
			START_SHIM(creat);
			START_SHIM(creat64);
			START_SHIM(close);
			START_SHIM(unlink);
			START_SHIM(unlinkat);
			START_SHIM(read);
			START_SHIM(write);
			START_SHIM(_llseek);
			START_SHIM(lseek64);
			START_SHIM(lseek);

			START_SHIM(clearerr);
			START_SHIM(setvbuf);
			START_SHIM(fopen);
//			START_SHIM(fopen64);
			START_SHIM(fdopen);
			START_SHIM(fclose);
			START_SHIM(fgets);
			START_SHIM(fputs);
			START_SHIM(vfprintf);
			START_SHIM(fflush);
			START_SHIM(ftell);
			START_SHIM(fseek);
			START_SHIM(fseeko);
			START_SHIM(fread);
			START_SHIM(fwrite);
			START_SHIM(fputc);
			START_SHIM(putc);
			START_SHIM(_IO_getc);
			START_SHIM(_IO_putc);
			START_SHIM(_IO_feof);
			START_SHIM(_IO_ferror);
			START_SHIM(fgetc);
			START_SHIM(getc);
			START_SHIM(feof);
			START_SHIM(ferror);
			START_SHIM(fileno);
			START_SHIM(fflush_unlocked);
			START_SHIM(fgets_unlocked);
			START_SHIM(fcntl);
			START_SHIM(fsync);
		}

		void stopFileShims()
		{
			STOP_SHIM(fsync);
			STOP_SHIM(fcntl);
			STOP_SHIM(fgets_unlocked);
			STOP_SHIM(fflush_unlocked);
			STOP_SHIM(fileno);
			STOP_SHIM(ferror);
			STOP_SHIM(feof);
			STOP_SHIM(getc);
			STOP_SHIM(fgetc);
			STOP_SHIM(putc);
			STOP_SHIM(fputc);
			STOP_SHIM(_IO_getc);
			STOP_SHIM(_IO_putc);
			STOP_SHIM(_IO_feof);
			STOP_SHIM(_IO_ferror);
			STOP_SHIM(fwrite);
			STOP_SHIM(fread);
			STOP_SHIM(fseeko);
			STOP_SHIM(fseek);
			STOP_SHIM(ftell);
			STOP_SHIM(fflush);
			STOP_SHIM(vfprintf);
			STOP_SHIM(fputs);
			STOP_SHIM(fgets);
			STOP_SHIM(fclose);
			STOP_SHIM(fdopen);
//			STOP_SHIM(fopen64);
			STOP_SHIM(fopen);
			STOP_SHIM(setvbuf);
			STOP_SHIM(clearerr);

			STOP_SHIM(lseek);
			STOP_SHIM(lseek64);
			STOP_SHIM(_llseek);
			STOP_SHIM(write);
			STOP_SHIM(read);
			STOP_SHIM(creat64);
			STOP_SHIM(creat);
			STOP_SHIM(open64);
			STOP_SHIM(open);
			STOP_SHIM(openat64);
			STOP_SHIM(openat);
			STOP_SHIM(unlinkat);
			STOP_SHIM(unlink);
			STOP_SHIM(close);
		}
	}
}
