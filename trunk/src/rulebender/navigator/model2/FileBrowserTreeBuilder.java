package rulebender.navigator.model2;

import java.io.File;

public class FileBrowserTreeBuilder 
{	
	public static BNGModelCollection buildTree(File modelDirectory)
	{
		if(!modelDirectory.exists() || !modelDirectory.isDirectory())
			return null;

		
		BNGModelCollection root = new BNGModelCollection();
		
		for(File f : modelDirectory.listFiles())
		{
			// If it's a bngl file
			if(f.getName().contains(".") && f.getName().substring(f.getName().lastIndexOf(".") + 1, f.getName().length()).equals("bngl"))
			{
				
				BNGModelNode modelNode = new BNGModelNode(root, f.getName().substring(0, f.getName().lastIndexOf(".")));
				
				FileNode fileNode = new FileNode(modelNode, f);
				
				modelNode.setBNGLFileNode(fileNode);
				
				root.addModel(modelNode);
			}
		}
		
		return root;
	}
}
