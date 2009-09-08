package edu.virginia.vcgr.genii.ui.shell;

public interface BindingActionListener
{
	public void beep();
	
	public void clear();
	
	public void addCharacter(char c);
	
	public void backspace();
	public void delete();
	
	public void left();
	public void right();
	
	public void end();
	public void home();
	
	public void backwardHistory();
	public void forwardHistory();
	
	public void search();
	public void stopSearch();
	
	public void complete();
	
	public void enter();
}