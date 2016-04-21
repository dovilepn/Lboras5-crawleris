import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

/**
 * Created by dovile on 2016-04-19.
 */
public class Crawler extends WebCrawler

{
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("http://www.15min.lt/");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();




            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());

            try {
                parseHTML(url);

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void writeText(String text, String name) throws IOException {
        Writer writer = null;

        boolean s = false;
        int number =1;
        while(s!=true){

            String fileName = "./data/"+name+".txt";
            File yourFile = new File(fileName);

            if (!yourFile.exists()) {
                yourFile.createNewFile();
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(yourFile), "utf-8"));
                writer.write(text);
                writer.flush();
                writer.close();
                s=true;

            }else {
                number++;
                //System.out.println(yourFile.getAbsolutePath());
            }

        }




    }
    private void parseHTML(String url) throws IOException{

        Document doc = Jsoup.connect(url).get();
        String authorName = "";
        String date = "";
        /*STRAIPSNIS*/
        //Tema
       //Autorius
        Element authorDIV = doc.select("div.author-info").first();
        if (authorDIV!=null){
            authorName = authorDIV.tagName("a.name").text();
        }


        //Pavadinimas
        String h1 = doc.select("h1").text();
        String articalName = h1.substring(0,20)+doc.select("div").attr("data-id");

        //Data
        Element dateMeta = doc.select("meta[itemprop=datePublished]").first();

        if (dateMeta!=null){
            date = dateMeta.attr("content");
        }

        //Laikas
        String time = doc.select("span.published").text();
        //Tekstas
        String articalText = doc.select("div.article_content p").text();
        /*Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");*/


        if(h1!=null && articalText!=null){//straipsnis
            String articleInfo = url+"\n"+"Autorius: "+authorName+"\n"+"Pavadinimas: "+
                    h1+"\n"+"Laikas: "+time+"\n"+"Data: "+date+"\n"+"Straipsnio tekstas: "+articalText+"\n";
            writeText(articleInfo,articalName);
        }



        /*KOMENTARAS*/

        List<String[]> comments = new ArrayList<String[]>();
        Element isComment = doc.select("div.comments-page").first();


        if (isComment!=null){//komentaru puslapis

            String articleID = doc.select("div").attr("data-id");

            Elements commentsDIV= doc.select("div.item.clearfix.main-item");
            for (Element div : commentsDIV) {

                String[] commentInfo = new String[6];

                //Numeris
                commentInfo[0] = div.attr("data-item");
                //Autorius
                commentInfo[1]= doc.select("span.name").text();
                //Data
                commentInfo[2] = doc.select("span.timestamp").attr("title");
                //Laikas
                commentInfo[3] = doc.select("span.timestamp").text();
                //IP
                commentInfo[4] = doc.select("span.name").attr("title");
                //Tekstas
                commentInfo[5] = doc.select("div.comment").text();

                comments.add(commentInfo);

            }
           for (String[] l:comments){

               String commentInfo = "";
               for (int i =0;i< l.length;i++){

                   commentInfo = url+"\n"+"Autorius: "+l[1]+"\n"+"Numeris: "+
                           l[0]+"\n"+"Laikas: "+l[3]+"\n"+"Data: "+l[2]+"\n"+"IP: "+l[4]+"\n"
                   +"Komentaro tekstas: "+l[5]+"\n";


               }
               if (!commentInfo.isEmpty()){
                   writeText(commentInfo,"komentaras"+comments.indexOf(l)+"tekstoID:"+articleID);
               }


           }





        }







    }
}
