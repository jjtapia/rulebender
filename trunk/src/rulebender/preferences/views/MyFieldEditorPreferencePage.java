package rulebender.preferences.views;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import rulebender.Activator;

public class MyFieldEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage 
{
	
	public MyFieldEditorPreferencePage() 
	{
		super(GRID);
	}

	public void createFieldEditors() {
		addField(new DirectoryFieldEditor("SIM_PATH", "&Path to simulator root (contains bin/):", getFieldEditorParent()));
		
		/*
		addField(new BooleanFieldEditor("BOOLEAN_VALUE", "&An example of a boolean preference", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor("CHOICE", "An example of a multiple-choice preference", 1,
				new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } }, getFieldEditorParent()));
		
		addField(new StringFieldEditor("MySTRING1", "A &text preference:",
				getFieldEditorParent()));
		
		addField(new StringFieldEditor("MySTRING2", "A &text preference:",
				getFieldEditorParent()));		
		*/
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}

}
