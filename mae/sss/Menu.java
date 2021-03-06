package mae.sss;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.lang.reflect.Array;
import java.util.zip.ZipFile;
//import java.applet.Applet; deleted V1.68
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import mae.util.TreePanel;
import mae.util.SimpleFilter;
import mae.util.Console;
import mae.util.Scaler;
import mae.brow.Fide;
import mae.brow.Browser;

/** Adds various static methods to SSS */
public class Menu {

    static JFrame frm;  //used in FileDialog
    static final String TXT_FLTR = "*.txt", SER_FLTR = "*.ser";
    /*static final SimpleFilter 
        TXT_FLTR = new SimpleFilter("txt", "Plain text files"),
        SER_FLTR = new SimpleFilter("ser", "Serialized object files");
    */
    Menu() { } //should be invisible

    /** (Chooser actions -- see {@link mae.sss.Chooser}) */
    public static Class chooser() throws ClassNotFoundException {
        return Class.forName("mae.sss.Chooser");
    }
    /** Display System.class */
    public static Class system() {  //V2.07
        return System.class;
    }
 
    static void showFrame(Frame c) {
        if (!c.isVisible()) c.setVisible(true);
        c.setState(Frame.NORMAL); c.toFront();
    }
    /** Displays the Console, if hidden */
    static Class console() {  //V2.05
        showFrame(Console.getInstance());
        Console.start();  //V2.07
        return null;
    }
    /** Opens Fide as the editor */
    public static Class editor() {  //V1.64
        if (Fide.instance == null) Fide.main();
        showFrame((Frame)toWindow(Fide.instance));
        return null;
    }
    /** Prints the object into system output */
    public static void println(Object x) {
        System.out.println(x);
    }
    /*
     * Sort the array in ascending order. 
     * Objects need not be Comparable, they
     * are sorted as Strings   */
    public static Object[] sort(Object[] a) {  //V1.65
        Arrays.sort(a, new Inspector.Comp());
        return a;
    }
    /** Display installed screens */
    public static void reportScreens() {  //V1.68
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] a = ge.getScreenDevices();
            String s = "Resolution = "+Scaler.RESOLUTION+"\n"+a.length+" devices\n"; 
            for (GraphicsDevice gd : a) 
               for (GraphicsConfiguration gc : gd.getConfigurations()) 
                  s += rectToStr(gc.getBounds())+"\n";
            System.out.println(s);
    }
        static String rectToStr(Rectangle r) {
            return "(x="+r.x+", y="+r.y+", w="+r.width+", h="+r.height+")";
        }
    
    /**
     * Chooses a serial file for reading objects 
     * @throws IOException
     */
    public static Object retrieveObjects() throws IOException {
        File f = Console.fileToOpen(SER_FLTR);
        InputStream is = new FileInputStream(f);
        ObjectInput in = new ObjectInputStream(is); 
        List lst = new ArrayList();
        try {
            while (is.available() > 0) 
                lst.add(in.readObject());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        in.close();
        switch (lst.size()) {
            case 0 : return null;
            case 1 : return lst.get(0);
            default: return lst.toArray();
        }
    }
    /**
     * Make a serial file for writing objects
     * @throws IOException
     */
    public static void saveObjects(Serializable[] a) throws IOException {
        File f = Console.fileToSave(SER_FLTR);
        if (f == null) return;
        OutputStream os = new FileOutputStream(f);
        ObjectOutput out = new ObjectOutputStream(os); 
        for (int i=0; i < a.length; i++) 
            out.writeObject(a[i]);
        out.close();
    }
    /** Reads a text file into a String */
    public static String toString(File f) throws IOException {
        return toString(new FileInputStream(f));
    }
    /**
     * Reads an InputStream into a String. Useful after URL.openStream() or
     * Class.getResourceAsStream()
     */
    public static String toString(InputStream in) throws IOException {
        StringBuffer s = new StringBuffer();
        int n = in.available();
        while (n > 0) {
            if (n == 1) {
                int c = in.read();
                if (c >= 0)
                    s.append((char) c);
            } else {
                byte[] b = new byte[n];
                n = in.read(b);
                if (n > 0)
                    s.append(new String(b, 0, n));
            }
            n = in.available();
        }
        return new String(s);
    }
    /** Saves a String into a text file */
    public static void saveText(String txt) {
        File f = Console.fileToSave(TXT_FLTR);
        if (f == null) return; 
        Console.saveToFile(txt, f);
    }
    /* 
     * @deprecated
     * Shows the contents of an Enumeration as an array of Objects  */
        @Deprecated
    public static Object[] toArray(Enumeration e) {
        List L = new ArrayList();
        while (e.hasMoreElements())
            L.add(e.nextElement());
        return L.toArray();
    }
       
    /* 
     * @deprecated
     * Converts an Enumeration to an Iterator  */
        @Deprecated
        public static Iterator toIterator(final Enumeration e) {  //V1.66
            return new Iterator() {
               public boolean hasNext() { return e.hasMoreElements(); }
               public Object next() { return e.nextElement(); }
               public void remove() { //unsupported operation
                  throw new UnsupportedOperationException("remove");
               }
            };
        }
        
    /** Shows the contents of an Iterator as an array of Objects */
    public static Object[] toArray(Iterator e) {
        List L = new ArrayList();
        while (e.hasNext())
            L.add(e.next());
        return L.toArray();
    }

    /**
     * Shows the contents of a directory, or a zip/jar file.
     * <P>
     * If f is a zip/jar archive, its contents are displayed. <BR>
     * Otherwise contents of the containing folder are displayed. <BR>
     * Returns a File array or a ZipEntry array.
     * 
     * @throws IOException
     */
    public static File[] toTree(File f) throws java.io.IOException {
        TreePanel p;
        String ext = SimpleFilter.extension(f); //may be null
        if ("zip".equals(ext) || "jar".equals(ext)) {
            p = new TreePanel(new ZipFile(f));
        } else {
            if (!f.isDirectory())
                f = f.getParentFile();
            p = new TreePanel(f);
        }
        TreePanel.show(p);
        List L = p.getList();
        File[] a = new File[L.size()];
        L.toArray(a);
        return a;
    }

    /** Shows the contents of a TreeNode; returns an array of nodes */
    public static TreeNode[] toTree(TreeNode n) {
        TreePanel.show(new TreePanel(n));
        ArrayList L = new ArrayList();
        addNodes(L, n);
        TreeNode[] a = new TreeNode[L.size()];
        L.toArray(a);
        return a;
        //return (TreeNode[])L.toArray(a);
    }
    static void addNodes(ArrayList L, TreeNode x) {
        L.add(x);
        int n = x.getChildCount();
        for (int i = 0; i < n; i++) {
            addNodes(L, (TreeNode) x.getChildAt(i));
        }
    }

    /**
     * Shows the containment hierarchy of Container c (possibly JComponent).
     * <P>
     * Returns an array of Components within c.
     */
    public static Component[] toTree(Container c) {
        TreePanel p = new TreePanel(c);
        TreePanel.show(p);
        List L = p.getList();
        Component[] a = new Component[L.size()];
        L.toArray(a);
        return a;
        //return (Component[])L.toArray(a);
    }
    /**
     * If the Component is already on a Window, returns it.
     * <PRE>
     * Otherwise, adds it into a Jframe. 
     * Invokes pack() and setVisible(true) on the new JFrame.
     * </PRE>
     */
    public static Window toWindow(Component c) {  //renamed V1.65
        /* else if (c.getParent() != null) {
            while (c.getParent() != null)c = c.getParent();
            return (Frame)c; } */ 
        if (c instanceof Window) return (Window)c;
                Window w = SwingUtilities.getWindowAncestor(c); 
                if (w != null) return w;
        //if (c instanceof Applet) ((Applet)c).init();
        JFrame frm = new JFrame(c.getClass().getName());
        frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (c instanceof Container
                &&((Container)c).getComponentCount()>0)
             frm.setContentPane((Container)c);
        else frm.getContentPane().add(c);
        frm.pack();
        frm.setVisible(true);
        //if (c instanceof Applet) ((Applet)c).start();
        return frm;
    }

        /** Makes a new Array of type t and size n) */
        public static Object newArray(String t, int n) {
            if (t.equals("boolean")) return new boolean[n];
            if (t.equals("char")) return new char[n];
            if (t.equals("byte")) return new byte[n];
            if (t.equals("short")) return new short[n];
            if (t.equals("int")) return new int[n];
            if (t.equals("long")) return new long[n];
            if (t.equals("float")) return new float[n];
            if (t.equals("double")) return new double[n];
            return makeArray(t, n); //recursive call
        }
        static Object makeArray(String t, int n) {
            try { 
                return Array.newInstance(Class.forName(t), n); 
            } catch (ClassNotFoundException e) {
                if (t.indexOf('.')<0) return makeArray("java.lang."+t, n);
            }
            throw new RuntimeException(t+" unknown type");
        }
    /** Shows SSS version (splash screen) */
    public static Class version() {  //V1.64
        InspectorPanel.showAboutDlg(); 
        return null;
    }

	/**
	Sometimes, Java compiler may find an error on Linux because of
	the java files encoded with Cp1254 (generally written in Windows). 
	The method below, converts the encoding of given files from Cp1254 to UTF-8.
	*/
	public static void fixEncoding(File... files){  //V2.10 (by B E Harmansa)
		//File[] files = Console.filesToOpen(null);

		final String baseEncoding = "Cp1254";
		final String targetEncoding = "UTF-8";

		for (File f: files) {
			try {
				InputStream is = new FileInputStream(f);
				byte[] baseData = new byte[(int)f.length()];
				is.read(baseData);  //if first char is BOM, file is UTF-8
				if (baseData[0]==-17 && baseData[1]==-69 && baseData[2]==-65) continue;

				String text = new String(baseData, baseEncoding);
				if (!Browser.isPlainText(f, text)) continue;

				byte[] targetData = text.getBytes(targetEncoding);
				if (targetData.length == f.length()) continue;

				OutputStream os = new FileOutputStream(f);
				os.write(-17); os.write(-69); os.write(-65); //write BOM
				os.write(targetData);

				System.out.printf("%s - %d byte(Cp1254) to %d byte(UTF-8)\n", 
									f.getName(), baseData.length, targetData.length);

			} catch (UnsupportedEncodingException ex) {
				System.err.println("Encoding not found");
			} catch (IOException ex ) {
				System.err.println("IOException occured");
			} 
		}
	}
}
