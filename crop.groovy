@Grab('com.itextpdf:itextpdf:5.3.4')
@Grab('org.apache.pdfbox:pdfbox:1.8.4')
@GrabConfig(systemClassLoader = true)
import java.io.FileOutputStream;
import java.io.IOException;
 
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfRectangle;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName
import java.util.*
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.pdfbox.pdmodel.PDDocument;

LowerX = 145
UpperX = 355

LowerAY = 465
UpperAY = 484

LowerFY = 484
UpperFY = 504

frect = new PdfRectangle(LowerX,LowerFY,UpperX,UpperFY);
arect = new PdfRectangle(LowerX,LowerAY,UpperX,UpperAY);

def writeImage(String theFile,String name) {
  PDDocument document = PDDocument.load(theFile);
  PDFImageWriter imageWriter = new PDFImageWriter(); 
  imageWriter.writeImage(document, "png", null,
                        1, 1, "generated/${name}");
  document.close()
  new File(theFile).delete()
}

def cropFile(String theFile) {
  inFile = new File(theFile)
  name = inFile.getName().hashCode()
  reader = new PdfReader(theFile);
  parser = new PdfReaderContentParser(reader);
  fnFileName = 'f' + "fn_${name}".hashCode()
  String fnFile = "generated/${fnFileName}.pdf"
  stamper = new PdfStamper(reader, new FileOutputStream(fnFile));
  pageDict = reader.getPageN(1)
  pageDict.put(PdfName.CROPBOX, frect);
  stamper.close();
  writeImage(fnFile,fnFileName)

  reader = new PdfReader(theFile);
  anFileName = 'f' + "an_${name}".hashCode()
  String anFile = "generated/${anFileName}.pdf"
  stamper = new PdfStamper(reader, new FileOutputStream(anFile));
  pageDict = reader.getPageN(1)
  pageDict.put(PdfName.CROPBOX, arect);
  stamper.close();
  writeImage(anFile,anFileName)

  return new Tuple("${fnFileName}.png","${anFileName}.png")
}

PdfFilter_ = new FileFilter() {
  def boolean accept(File pathName) {
    return pathName.getName().endsWith(".pdf") || pathName.isDirectory()
  }
}

def searchFiles(File rootDir, List<String> res) {
  for (File file_ : rootDir.listFiles(PdfFilter_)) {
    if (file_.isDirectory()) {
      searchFiles(file_,res)
    } else {
      res.add(file_)
    }
  }
  return
}

files = new ArrayList<String>()
searchFiles(new File("./"),files)
new File("db.txt").withWriter { out -> 
  files.eachWithIndex { f,i ->
    if (i > 10) System.exit(0)
    tuple = cropFile(f.getCanonicalPath())
    out.println "${f.getName()},${tuple.get(0)},${tuple.get(1)}"
  }
}

