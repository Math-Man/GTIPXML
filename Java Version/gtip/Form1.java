package gtip;

import java.text.Normalizer;
import java.awt.EventQueue;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import java.awt.GridLayout;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Form1 {

	private JFrame frmGtip;
	private JTextField tfDirectory;
	private JTextField tfSearchCodeDesc;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Form1 window = new Form1();
					window.frmGtip.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Form1() throws IOException, ParserConfigurationException, SAXException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGtip = new JFrame();
		frmGtip.setTitle("GTIP");
		frmGtip.setResizable(false);
		frmGtip.setBounds(100, 100, 431, 387);
		frmGtip.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGtip.getContentPane().setLayout(null);
		
		ArrayList<Triplet> TripletsList = new ArrayList<Triplet>();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPane.setBounds(197, 162, 207, 177);
		frmGtip.getContentPane().add(scrollPane);
		
		JTextArea taInfo = new JTextArea();
		scrollPane.setViewportView(taInfo);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPane_1.setBounds(10, 163, 177, 176);
		frmGtip.getContentPane().add(scrollPane_1);
		
		DefaultCaret caret = (DefaultCaret)taInfo.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		
		JTree jtvCodesTree = new JTree();
		jtvCodesTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) 
			{
				if(arg0.getKeyCode() == KeyEvent.VK_DELETE && jtvCodesTree.getSelectionPath() != null) 
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)jtvCodesTree.getSelectionPath().getLastPathComponent();
					if (node.getParent() != null) {
                        ((DefaultTreeModel) jtvCodesTree.getModel()).removeNodeFromParent(node);
                    }
				}
			}
		});
		scrollPane_1.setViewportView(jtvCodesTree);
		jtvCodesTree.setRootVisible(false);
		jtvCodesTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				//System.out.println(jtvCodesTree.getSelectionPath().getLastPathComponent());
				if(jtvCodesTree.getSelectionPath() != null) 
				{
					String lastSelectedDir = jtvCodesTree.getSelectionPath().getLastPathComponent().toString();
					Triplet result = findEntryByCode(lastSelectedDir, TripletsList);
					taInfo.setText("CODE: " + result.getFirst() + "\nDESCRIPTION: " + result.getSecond() + "\nDIRECTORY: " + result.getThird());
				}
				
			}
		});
		jtvCodesTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Root") {{ }}
		));
		
		tfDirectory = new JTextField();
		tfDirectory.setText("D:\\Projects\\jguar_GIT_Set\\jprod\\UnityServer\\WebContent\\resources\\TRTR");
		tfDirectory.setBounds(10, 25, 264, 20);
		frmGtip.getContentPane().add(tfDirectory);
		tfDirectory.setColumns(10);
		
		tfSearchCodeDesc = new JTextField();
		tfSearchCodeDesc.setBounds(10, 92, 394, 20);
		frmGtip.getContentPane().add(tfSearchCodeDesc);
		tfSearchCodeDesc.setColumns(10);	
		
		JLabel lblSearchDirectoryFor = new JLabel("Search Directory for XML files:");
		lblSearchDirectoryFor.setBounds(10, 11, 198, 14);
		frmGtip.getContentPane().add(lblSearchDirectoryFor);
		
		JLabel lblCodeOrDescription = new JLabel("Code or Partial Description:");
		lblCodeOrDescription.setBounds(10, 78, 177, 14);
		frmGtip.getContentPane().add(lblCodeOrDescription);
		
		JLabel lbfCount = new JLabel("");
		lbfCount.setBackground(SystemColor.menu);
		lbfCount.setBounds(10, 53, 264, 14);
		frmGtip.getContentPane().add(lbfCount);
		
		JButton btnSearchXml = new JButton("Search XML");
		btnSearchXml.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				TripletsList.clear();
				try {
					List<String> xmls = findXMLs(tfDirectory.getText());
					ArrayList<Triplet> tuples = genTuples(tfDirectory.getText());
					
					if(xmls.size() == 0) {lbfCount.setText("No GTIP XML files found.");}
					else if(tuples.size() == 0) {lbfCount.setText("No Proper entries found in " + xmls.size() + " GTIP XML files.");}
					else{lbfCount.setText("Found: " + xmls.size() + " GTIP XML files and " + tuples.size() + " Entries."); TripletsList.addAll(tuples);}
					
				} catch (IOException | ParserConfigurationException | SAXException  e ) {
					e.printStackTrace();
				}
			}
		});
		btnSearchXml.setBounds(284, 41, 120, 26);
		frmGtip.getContentPane().add(btnSearchXml);
		
		
		JButton btnSearchDescription = new JButton("Search Description");
		btnSearchDescription.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				Triplet result = findEntryByDescription(tfSearchCodeDesc.getText(), TripletsList);
				
				if(result == null) {lbfCount.setText("No Proper entries found for given description!");}
				
				taInfo.setText("CODE: " + result.getFirst() + "\nDESCRIPTION: " + result.getSecond() + "\nDIRECTORY: " + result.getThird());
				
				ArrayList<Triplet> entryTree = genEntryTree((String)result.getFirst(), TripletsList);
				
				DefaultTreeModel model = (DefaultTreeModel)jtvCodesTree.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
				DefaultMutableTreeNode lastNode = root;
				
				for(Triplet trip : entryTree)
				{
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(trip.getFirst());
					lastNode.add(newNode);
					lastNode = (newNode);
					model.reload(root);
				}
			}
		});
		btnSearchDescription.setBounds(197, 116, 207, 36);
		frmGtip.getContentPane().add(btnSearchDescription);
		
		JButton btnSearchCode = new JButton("Search Code");
		btnSearchCode.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				
				Triplet result = findEntryByCode(tfSearchCodeDesc.getText(), TripletsList);
				
				if(result == null) {lbfCount.setText("No Proper entries found for given code!");}
				else 
				{
					taInfo.setText("CODE: " + result.getFirst() + "\nDESCRIPTION: " + result.getSecond() + "\nDIRECTORY: " + result.getThird());
					
					ArrayList<Triplet> entryTree = genEntryTree(tfSearchCodeDesc.getText(), TripletsList);
					
					DefaultTreeModel model = (DefaultTreeModel)jtvCodesTree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
					DefaultMutableTreeNode lastNode = root;
					
					for(Triplet trip : entryTree)
					{
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(trip.getFirst());
						lastNode.add(newNode);
						lastNode = (newNode);
						model.reload(root);
					}
				}
				
			}
		});
		btnSearchCode.setBounds(10, 116, 177, 36);
		frmGtip.getContentPane().add(btnSearchCode);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(-17, 72, 443, 2);
		frmGtip.getContentPane().add(separator);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(Paths.get(tfDirectory.getText()).toFile());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//fc.showOpenDialog(frmGtip);

				if (fc.showOpenDialog(frmGtip) == JFileChooser.APPROVE_OPTION) { 
					 tfDirectory.setText(fc.getSelectedFile()+"");
				 }
				 else {
				      //System.out.println("No Selection ");
				  }	
			}
		});
		btnBrowse.setBounds(284, 7, 120, 26);
		frmGtip.getContentPane().add(btnBrowse);
	}

	private List<String> findXMLs(String scanDir) throws IOException {
		// https://stackoverflow.com/a/43952002
		Stream<Path> paths = Files.walk(Paths.get(scanDir));
		try {
			List<String> files = paths.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().toLowerCase().startsWith("gtip"))
					.filter(p -> p.getFileName().toString().toLowerCase().contains(".xml")).map(p -> p.toString())
					.collect(Collectors.toList());
			return files;
		} finally {
			if (null != paths) {
				paths.close();
			}
		}
		
	}

	private ArrayList<Triplet> genTuples(String scanDir) throws IOException, ParserConfigurationException, SAXException {
			
		List<String> allFiles = findXMLs(scanDir);
		ArrayList<Triplet> Tuples = new ArrayList<Triplet>();

		for (String file : allFiles) {
			
			String currentCode = "";
			String currentDescription = "";
			String currentFile = file;

			File inputFile = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("GTIP");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					for (int j = 0; j < eElement.getChildNodes().getLength(); j++) {
						Node currentNode = eElement.getChildNodes().item(j);
						if (!(currentNode instanceof Text)) {
							if (currentNode.getNodeName().equals("CODE")) {currentCode = currentNode.getTextContent();}
							else if (currentNode.getNodeName().equals("DESCRIPTIONS")) {
								for (int k = 0; k < currentNode.getChildNodes().getLength(); k++) {
									if (!(currentNode instanceof Text)) {
										
										NodeList altNodes = currentNode.getChildNodes().item(k).getChildNodes();
										for (int t = 0; t < altNodes.getLength(); t++) {
											if (!(currentNode instanceof Text)) {
												if (altNodes.item(t).getNodeName().equals("DESCR")) {
													String str = altNodes.item(t).getTextContent();
													str = clearTurkishChars(str);
													currentDescription = str;
												}
											}
										}
										
									}
								}
							}
						}
					}
					
				}
				//Add all tuples from all files to the list
				Tuples.add(new Triplet<String, String, String>(currentCode, currentDescription, currentFile));
			}
		}
		return Tuples;
	}

	private Triplet findEntryByCode(String Code, ArrayList<Triplet> TuplesList)
	{
		for(Triplet t : TuplesList) 
		{
			if(t.getFirst().equals(Code)) 
			{
				return t;
			}
		}
		return null;
	}
	
	private ArrayList<Triplet> genEntryTree(String Code, ArrayList<Triplet> TuplesList) 
	{
			
		ArrayList<Triplet> treeList = new ArrayList<Triplet>();
			
		char lookFor = 0;
		if(Code.contains(" ")) {lookFor = ' ';}
		else if(Code.contains(".")) {lookFor = '.';}
		
		//Add initial code to the list
		Triplet firstEntry = findEntryByCode(Code, TuplesList);
		treeList.add(firstEntry);
		
		String code = Code;
		while(code.contains(" ") || code.contains("."))
		{
			code = code.substring(0, code.lastIndexOf(lookFor));
			Triplet entry = findEntryByCode(code, TuplesList);
			if(entry != null) 
			{
				treeList.add(entry);
			}		
		}
		return treeList;
	}

	private Triplet findEntryByDescription(String PartialDescription, ArrayList<Triplet> TuplesList)
	{
		return findBestMatch(TuplesList, PartialDescription);
	}

	public static String clearTurkishChars(String str) {
		String ret = str;
		char[] turkishChars = new char[] { 0x131, 0x130, 0xFC, 0xDC, 0xF6, 0xD6, 0x15F, 0x15E, 0xE7, 0xC7, 0x11F, 0x11E };
		char[] englishChars = new char[] { 'i', 'I', 'u', 'U', 'o', 'O', 's', 'S', 'c', 'C', 'g', 'G' };
		for (int i = 0; i < turkishChars.length; i++) {
			ret = ret.replaceAll(new String(new char[] { turkishChars[i] }),
					new String(new char[] { englishChars[i] }));
		}
		return ret;
	}
	
	public static Triplet findBestMatch(ArrayList<Triplet> sList, String target)
	{
		double best = Double.NEGATIVE_INFINITY;
		Triplet bestTriplet = null;
		for (Triplet t : sList)
		{
			if(t.getSecond() != null || t.getSecond().toString().length() != 0)
			{
				String str = t.getSecond().toString();
				str = str.replace("\n", "").replace("\r", "").replace("\r\n", "").replace("\n\r", "").replace("  ", " ");
				if(str.startsWith(" ")) {str = str.substring(1);}
				double val = LetterPairSimilarity.compareStrings(str, target);
				if(val > best)
				{
					bestTriplet = t;
					best = val;
				}
			}
			
		}
		return bestTriplet;
	}
}
