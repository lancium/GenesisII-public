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
}
