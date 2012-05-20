package visualizationviewer.annotation;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JTable;

import org.eclipse.swt.program.Program;

public class TableMouseListener implements MouseListener 
{
	public void mouseClicked(MouseEvent e) 
	{
		JTable table = (JTable) e.getSource();
		Point pt = e.getPoint();
		int crow = table.rowAtPoint(pt);
		int ccol = table.columnAtPoint(pt);
		
		// molecule name, the second column
		String molename = ((String) table.getValueAt(0, 1)).trim();
		String address = "";

		// column Name
		String columnContent = (String) table.getModel().getValueAt(crow, ccol);
		if (columnContent == null) {
			return;
		}
		
		// UniProt
		if (columnContent.equals("UniProt")) {		
			address = "http://www.uniprot.org/uniprot/?query=";
			address += molename;
			address += "&sort=score";
		}
		// PathwayCommon
		if (columnContent.equals("PathwayCommons")) {
			address = "http://www.pathwaycommons.org/pc/webservice.do?version=3.0&snapshot_id=GLOBAL_FILTER_SETTINGS&record_type=PATHWAY&q=";
			address += molename;
			address += "&format=html&cmd=get_by_keyword";
		}
		// HPRD
		else if (columnContent.equals("HPRD")) {
			address = "http://www.hprd.org/resultsQuery?multiplefound=&prot_name=";
			address += molename;
			address += "&external=Ref_seq&accession_id=&hprd=&gene_symbol=&chromo_locus=&function=&ptm_type=&localization=&domain=&motif=&expression=&prot_start=&prot_end=&limit=0&mole_start=&mole_end=&disease=&query_submit=Search";
		}
		// Reactome
		else if (columnContent.equals("Reactome")) {
			address = "http://www.reactome.org/cgi-bin/search2?OPERATOR=ALL&SPECIES=48887&QUERY=";
			address += molename;
		}
		// UCSD-Nature
		else if (columnContent.equals("UCSD-Nature")) {
			address = "http://www.signaling-gateway.org/molecule/search?nm=";
			address += molename;
		}
		// InterPro
		else if (columnContent.equals("InterPro")) {
			address = "http://www.ebi.ac.uk/interpro/ISearch?query=";
			address += molename;
		}
		// PROSITE
		else if (columnContent.equals("PROSITE")) {
			address = "http://au.expasy.org/cgi-bin/prosite-search-ful?SEARCH=";
			address += molename;
		}
		// KEGG
		else if (columnContent.equals("KEGG")) {
			address = "http://www.genome.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=kegg&keywords=";
			address += molename;
		}
		// ChEBI
		else if (columnContent.equals("ChEBI")) {
			address = "http://www.ebi.ac.uk/chebi/advancedSearchFT.do?searchString=";
			address += molename;
			address += "&queryBean.stars=3&queryBean.stars=-1";
		}
		// PubChem
		else if (columnContent.equals("PubChem")) {
			address = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=pccompound&term=";
			address += molename;
		}
		
		if (!address.equals("")) {
			// link to address
			
			// This breaks java 1.5 compatibility 
		
			/* 
			java.net.URI uri;
 
			try {
				uri = new java.net.URI(address);
				java.awt.Desktop.getDesktop().browse(uri);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			*/
			
			// This does not break java 1.5 compatibility
			Program.launch(address);
		}

	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}	
}
