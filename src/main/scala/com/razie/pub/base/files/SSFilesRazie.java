/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package com.razie.pub.base.files;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.razie.pub.FileUtils;
import com.razie.pub.base.log.Log;
//import com.razie.pub.base.log.Log;
import com.razie.pubstage.data.JStrucList;
import com.razie.pubstage.data.JStrucTree;
import com.razie.pubstage.data.JStructure;
import com.razie.pubstage.data.TreeImplNode;
import com.razie.pubstage.life.*;

/**
 * utility helper for working with files - especially finding files
 * 
 * @author $Author: razvanc $
 */
public class SSFilesRazie {

    public static interface FileFoundCback {
        void fileFound(File file);
    }

    /**
     * @param start the path to look in
     * @param filter - a filter, see Reg* classes defined here
     * @param recurse if true will search recursively through directories
     * @return a list of xml file names in the given path with the given mask, having the root tag
     *         and root attribute matching an expression. It may be empty but not null
     */
    public static JStrucList<File> listFiles(String start, FileFilter filter, boolean recurse,
            FileFoundCback... cback) {
        JStrucList<File> l = new JStrucList.Impl<File>(null);

        ifindFiles(start, l, filter, recurse, cback);

        return l;
    }

    /**
     * @param start the path to look in
     * @param filter - a filter, see Reg* classes defined here
     * @param recurse if true will search recursively through directories
     * @return a list of xml file names in the given path with the given mask, having the root tag
     *         and root attribute matching an expression. It may be empty but not null
     */
    public static JStrucTree<File> treeFiles(String start, FileFilter filter, boolean recurse,
            FileFoundCback... cback) {
        JStrucTree<File> l = new JStrucTree.ImplNode<File>(null);

        ifindFiles(start, l, filter, recurse, cback);

        return l;
    }

    /**
     * @param start the path to look in
     * @param filter - a filter, see Reg* classes defined here
     * @param recurse if true will search recursively through directories
     * @return a list of xml file names in the given path with the given mask, having the root tag
     *         and root attribute matching an expression. It may be empty but not null
     */
    public static void ifindFiles(String start, JStructure<File> l, FileFilter filter, boolean recurse,
            FileFoundCback... cback) {
        if (start == null)
            return;

        String dir = start;
        if (!dir.endsWith("/") && !dir.endsWith("\\")) {
            dir += "/";
        }

        File f = new File(dir);

        if (f.isDirectory() && l instanceof JStrucTree)
            ((JStrucTree.ImplNode<File>) l).setContents(f);

        findFiles(f, l, filter, recurse, 2, 100, cback);

        Log.logThis(l.toString());
    }

    private static JStructure<File> add(JStructure<File> l, File f, boolean isDirectory) {
        if (l instanceof JStrucTree) {
            if (isDirectory)
                return ((JStrucTree.ImplNode<File>) l).addNode(f);
            else
                ((JStrucTree.ImplNode<File>) l).addLeaf(f);
        } else if (!isDirectory) {
            JStrucList.Impl<File> tree = (JStrucList.Impl<File>) l;
            tree.add(f);
            return tree;
        } else return l;
        return null;
    }

    /**
     * TODO SECURITY add a filter for the directory name
     * 
     * @param dir the directory to start the search in
     * @param files will add files to this list
     * @param filter the filter for files
     * @param recurse if true will search recursively
     */
    public static void findFiles(File dir, JStructure<File> files, FileFilter filter, boolean recurse,
            int min, int max, FileFoundCback... cback) {

        WorkerBase$.MODULE$.candienow();

        try {
            File[] s = filter != null ? dir.listFiles(filter) : dir.listFiles();
            if (s != null) {
                for (int i = 0; s != null && i < s.length; i++) {
                    add(files, s[i], false);
                    if (cback.length > 0) {
                        cback[0].fileFound(s[i]);
                    }
                }
            }

            // TODO optimize this so we only find directories
            if (recurse) {
                File[] dirs = dir.listFiles();
                if (dirs == null) {
                    return;
                }

                int noDirs = 0;
                for (int i = 0; i < dirs.length; i++) {
                    if (dirs[i].isDirectory()) {
                        noDirs++;
                    }
                }

                if (noDirs > 0) {
                    int curDirNo = 0;

                    for (int i = 0; i < dirs.length; i++) {
                        if (dirs[i].isDirectory()) {
                            findFiles(dirs[i], add(files, dirs[i], true), filter, recurse, min
                                    + (int) ((max - min) * (curDirNo * 1.0 / noDirs)), min
                                    + (int) ((max - min) * ((curDirNo + 1) * 1.0 / noDirs)), cback);
                            curDirNo++;
                            WorkerBase$.MODULE$.updateProgress(min + (int) ((max - min) * (curDirNo * 1.0 / noDirs)),
                                    dirs[i].getName());
                        }
                    }
                }
            }
        } catch (SecurityException ex) {
            // do nothing here
        }
    }

    /**
     * return nice string with filesize
     * 
     * @param size a file size in bytes
     * @return a nice string in K/M/G
     */
    public static String niceFileSize(long size) {
        float f = size;
        if (size > G) {
            return String.valueOf(formatFloat(f / G, 3)) + " Gb";
        } else if (size > M) {
            return String.valueOf(formatFloat(f / M, 3)) + " Mb";
        } else if (size > K) {
            return String.valueOf(formatFloat(f / K, 3)) + " Kb";
        } else {
            return String.valueOf(size) + " bytes";
        }
    }

    /**
     * format a float as string with the given no of digits
     * 
     * @param f the float to format
     * @param nbDigits no of digits after .
     * @return nice float string
     */
    private static String formatFloat(float f, int nbDigits) {
        String s = "" + f;
        int idx = s.indexOf('.');
        if ((idx >= 0) && (idx + nbDigits + 1 < s.length()))
            s = s.substring(0, idx + nbDigits + 1);
        return s;
    }

    public static String getRootAttrFromFile(File file, String rootAttrNm) {
        RootElementChecker handler = new RootElementChecker();
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(file, handler);
        } catch (FirstElementFoundException e) {
            // do nothing
        } catch (Exception e) {
            return null;
        }
        String attrVal = rootAttrNm == null ? null : handler.elementAttributes.getValue(rootAttrNm);
        return attrVal;
    }

    /** SAX handler to stop parsing at the first element and get its attributes */
    private static class RootElementChecker extends DefaultHandler {
        String     elementName;
        Attributes elementAttributes;

        public RootElementChecker() {
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            elementAttributes = attributes;
            elementName = qName;
            throw new FirstElementFoundException("First Element found");
        }
    }

    /** specific exception thrown when first element found */
    static class FirstElementFoundException extends SAXException {
        public FirstElementFoundException(String message) {
            super(message);
        }
    }

    /**
     * a regexp based file filter, will filter files based on their name, if it matches the given
     * regular expression
     */
    public static class ORFileFilter implements FileFilter, Cloneable {
        protected List<FileFilter> filters = new ArrayList<FileFilter>();

        public ORFileFilter() {
        }

        public ORFileFilter(ORFileFilter f) {
            this.filters.addAll(f.filters);
        }

        public boolean accept(File pathname) {
            for (FileFilter ff : filters) {
                if (ff.accept(pathname)) {
                    return true;
                }
            }
            return false;
        }

        public void add(FileFilter f) {
            this.filters.add(f);
        }

        public Object clone() {
            return new ORFileFilter(this);
        }

        public String toString() {
            return "SmpFiles.OrFileFilter " + Log.tryToString("", this.filters);
        }
    }

    public static class ANDFileFilter extends ORFileFilter {

        public ANDFileFilter() {
        }

        public ANDFileFilter(ANDFileFilter f) {
            super(f);
        }

        public boolean accept(File pathname) {
            for (FileFilter ff : filters) {
                if (!ff.accept(pathname)) {
                    return false;
                }
            }
            return true;
        }

        public Object clone() {
            return new ANDFileFilter(this);
        }

        public String toString() {
            return "SmpFiles.ANDFileFilter " + Log.tryToString("", this.filters);
        }
    }

    public static class NOTFileFilter implements FileFilter {
        FileFilter filter;

        public NOTFileFilter() {
        }

        public NOTFileFilter(NOTFileFilter f) {
            this.filter = f.filter;
        }

        public boolean accept(File pathname) {
            if (filter.accept(pathname)) {
                return false;
            }
            return true;
        }

        public void setFilter(FileFilter f) {
            filter = f;
        }

        public Object clone() {
            return new NOTFileFilter(this);
        }

        public String toString() {
            return "SmpFiles.NOTFileFilter " + Log.tryToString("", this.filter);
        }
    }

    /**
     * a regexp based file filter, will filter files based on their name, if it matches the given
     * regular expression
     */
    public static class RegExpFileFilter implements FileFilter {
        protected String  regExp;
        protected Pattern pattern;

        public RegExpFileFilter(String regExp) {
            this.regExp = regExp;
            pattern = Pattern.compile(regExp);
        }

        public boolean accept(File pathname) {
            boolean b = pathname.isFile() && pattern.matcher(pathname.getName()).matches();
            if (b && SSFilesRazie.logger.isTraceLevel(3)) {
                // SmpFilesRazie.logger.trace(3, "RegExpFileFilter accepted file: " +
                // pathname.getName());
            } else if ((!b) && SSFilesRazie.logger.isTraceLevel(3)) {
                // SmpFilesRazie.logger.trace(3, "RegExpFileFilter REJECTED file: " +
                // pathname.getName());
            }

            return b;
        }

        public Object clone() {
            return new RegExpFileFilter(regExp);
        }

        public String toString() {
            return "SmpFiles.RegExpFileFilter(regExp=" + this.regExp + ")";
        }
    }

    /**
     * a regexp based XML file filter, will filter files based on their name, if it matches the
     * given regular expression and if it contains a specific root tag and/or root attribute
     * name/value
     */
    public static class RegExpXmlFileFilter extends RegExpFileFilter implements FileFilter {
        private String    rootTag, rootAttrNm, rootAttrRegExp;

        /**
         * c-tor
         * 
         * @param regexp regexp for filename. cannot be null
         * @param rootTag root tag name - can be null (no check for root tag but will check for root
         *        attr)
         * @param rootAttrNm root attr name - can be null (no check for root attr)
         * @param rootAttrRegExp regexp for root attr value - can be null (no check for root attr)
         */
        public RegExpXmlFileFilter(String regexp, String rootTag, String rootAttrNm, String rootAttrRegExp) {
            super(regexp);
            this.rootTag = rootTag;
            this.rootAttrNm = rootAttrNm;
            this.rootAttrRegExp = rootAttrRegExp;
        }

        public String toString() {
            return "SmpFiles.RegExpXmlFileFilter(regExp=" + this.regExp + ", rootTag=" + this.rootTag
                    + ", rootAttrNm=" + rootAttrNm + ", rootAttrRegExp=" + this.rootAttrRegExp + ")";
        }

        public boolean accept(File pathname) {
            boolean b = pathname.isFile() && pattern.matcher(pathname.getName()).matches()
                    && checkRoot(pathname);
            if (b && SSFilesRazie.logger.isTraceLevel(1)) {
                SSFilesRazie.logger.trace(1, "RegExpXmlFileFilter accepted file: " + pathname.getName());
            } else if ((!b) && SSFilesRazie.logger.isTraceLevel(3)) {
                SSFilesRazie.logger.trace(3, "RegExpXmlFileFilter REJECTED file: " + pathname.getName());
            }

            return b;
        }

        public boolean checkRoot(File file) {
            RootElementChecker handler = new RootElementChecker();
            try {
                SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                parserFactory.setValidating(false);
                SAXParser parser = parserFactory.newSAXParser();
                parser.parse(file, handler);
            } catch (FirstElementFoundException e) {
                // do nothing
            } catch (Exception e) {
                return false;
            }
            String attrVal = rootAttrNm == null ? null : handler.elementAttributes.getValue(rootAttrNm);
            // TODO i don't handle rootAttrNm==null && rootAttrRegExp!=null, i.e. there is at least
            // an attribute matching the exp
            return (rootTag == null || rootTag.equals(handler.elementName))
                    && (rootAttrRegExp == null || ((attrVal != null) && attrVal.matches(rootAttrRegExp)));
        }

        public Object clone() {
            return new RegExpXmlFileFilter(regExp, rootTag, rootAttrNm, rootAttrRegExp);
        }

    }

    public static void main(String args[]) throws Exception {
        RegExpXmlFileFilter filter = new RegExpXmlFileFilter("", "script", "test", "vall.*");
        System.out.println(filter.checkRoot(new File("c:/work/gigi.xml")));
        String fn = "file:/c:/gugu\\./vasile/..\\ionel.xml";
        System.out.println("canonical (" + fn + ") = " + FileUtils.toCanonicalPath(fn));
        fn = "file:/c:/gugu\\./vasile\\ionel.xml";
        System.out.println("canonical (" + fn + ") = " + FileUtils.toCanonicalPath(fn));
    }

    /** formatting file sizes */
    private static float    K      = 1024;
    private static float    M      = K * 1024;
    private static float    G      = M * 1024;

    public static final Log logger = Log.factory.create("", SSFilesRazie.class.getName());

}
