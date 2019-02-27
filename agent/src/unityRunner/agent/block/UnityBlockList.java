package unityRunner.agent.block;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: clement.dagneau
 * Date: 16/12/2011
 * Time: 09:52
 */
public class UnityBlockList {
    static public List<Block> blocks;

    public static void ParseLines(java.io.File logBlockListFile) {
        if(!logBlockListFile.exists())
        {
            blocks = Arrays.asList(
                new PlayerStatisticsBlock(),
                new CompileBlock(),
                new PrepareBlock(),
                new LightmapBlock(),
                new UpdateBlock()
            );
            return;
        }

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(logBlockListFile);

             doc.getDocumentElement().normalize();

             System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

             NodeList lineList = doc.getElementsByTagName("block");
             for (int lineListIndex = 0; lineListIndex < lineList.getLength(); lineListIndex++)
             {
                 Node lineListNode = lineList.item(lineListIndex);

                 if (lineListNode.getNodeType() == Node.ELEMENT_NODE)
                 {
                     Element lineListElement = (Element)lineListNode;

                     String name = lineListElement.getAttribute("name");
                     String beginning = lineListElement.getAttribute("beginning");
                     String end = lineListElement.getAttribute("end");

                     if (name == null || name.equals(""))
                     {
                         System.out.println("name was null or empty when parsing");
                         continue;
                     }

                     if (beginning == null || beginning.equals(""))
                     {
                         System.out.println("beginning was null or empty when parsing");
                         continue;
                     }

                     if (end == null || end.equals(""))
                     {
                         System.out.println("end was null or empty when parsing");
                         continue;
                     }

                     blocks.add(new CustomBlock(name, beginning, end));
                 }
             }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
