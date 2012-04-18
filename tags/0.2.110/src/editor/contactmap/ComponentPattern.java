package editor.contactmap;

public class ComponentPattern 
{
	int compindex;
	int stateindex;
	int wildcards; // -1: None 0: ? 1: +
	ComponentPattern()
	{
		stateindex = -1;
		wildcards = -1;
	}
}
