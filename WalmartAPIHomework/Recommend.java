import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.lang.NullPointerException;

public class Recommend {
    /**
    * The Recommendation class is the data structure I designed to get product information.
    */
    public static class Recommendation {
        public String itemId;
        public String name;
        public float score;
        public Recommendation(String itemId, String name,float score){
            this.itemId = itemId;
            this.name = name;
            this.score = score;
        }
    }
        public static enum ErrorCode { 
            SEARCH_NOT_FOUND("search not found"), 
            RECOMMEND_NOT_FOUND("recommend not found"); 
        
            private String error;

            ErrorCode(String error) {
                this.error = error;
            }

            public String getError() {
                return error;
            }
        }
    
    private static String apiKey = "q3racac3yq3k2rys5razdyze";
    private static String urlSearchPrefix="http://api.walmartlabs.com/v1/search?apiKey=";
    private static String urlRecommendPrefix="http://api.walmartlabs.com/v1/nbp?apiKey=";
    private static String urlReviewPrefix="http://api.walmartlabs.com/v1/reviews/";
    /**
     * @param url of end point.
     * @return a string of json.
     * @exception IOException On input error.
     */
    
    public static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }
    
    /**
     * @param searchWord this is search string.
     * @return products according to search string.
     * @exception IOException On input error.
     * @exception NullPointerException if search result is None or empty.
     */
    public static JSONArray search(String searchWord){
        String url = urlSearchPrefix+apiKey+"&query="+searchWord;
        JSONArray items = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(httpGet(url));
            items = (JSONArray) json.get("items");
            if (items == null || items.size() == 0) {
                throw new NullPointerException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println(ErrorCode.SEARCH_NOT_FOUND.getError());
        }
        return items;
    }
    /**
     * @param itemId This is product id.
     * @param max This is the max number of recommendation we want to see.
     * @return recommendations according to product id.
     * @exception IOException On input error.
     * @exception NullPointerException if recommend result is None or empty.
     */
    public static List<Recommendation> recommend(String itemId,int max){
        String url = urlRecommendPrefix+apiKey+"&itemId="+itemId;
        List<Recommendation> res = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONArray items = (JSONArray) parser.parse(httpGet(url));
            if (items == null || items.size() == 0) {
                throw new NullPointerException();
            }
            for(int i =0;i<Math.min(items.size(),max);i++){
                JSONObject item = (JSONObject)items.get(i);
                String id= String.valueOf( item.get("itemId"));
                String name = String.valueOf( item.get("name"));
                float score = reviewScore(id);
                res.add(new Recommendation(id,name,score));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (NullPointerException e) {
             System.out.println(ErrorCode.RECOMMEND_NOT_FOUND.getError());
        }
        return res;
    }
    /**
     * @param itemId This is product id.
     * @return the score of this item in float format in the range from 0 to 1.
     * @exception IOException On input error.
     * @exception ParseException On parse method.
     */
    public static float reviewScore(String itemId){
        String url = urlReviewPrefix+itemId+"?apiKey="+apiKey+"&format=json";
        float totalScore = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(httpGet(url));
            JSONObject review = (JSONObject) json.get("reviewStatistics");
            if(review != null) {
                totalScore = Float.valueOf((String) review.get("averageOverallRating"));
                totalScore /= Float.valueOf((String) review.get("overallRatingRange"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return totalScore;
    }

    /**
    * This is the main method which makes use of all these method.
    * @param args Unused.
    * @return Nothing.
    */
    public static void main(String[] args) {
        JSONArray items =  search(args[0]);
        if(items!= null && items.size()>0) {
            JSONObject item = (JSONObject) items.get(0);
            String itemId = String.valueOf(item.get("itemId"));
            List<Recommendation> recommends = recommend(itemId,10);
            Collections.sort(recommends, new Comparator<Recommendation>() {
                @Override
                public int compare(Recommendation r1, Recommendation r2) {
                    float res = r2.score - r1.score;
                    if (res > 0) {
                        return 1;
                    } else if (res < 0) {
                        return -1;
                    } else {
                        return 0;
                    }

                }
            });
            for (int i = 0; i < recommends.size(); i++) {
                Recommendation re = recommends.get(i);
                System.out.println(i+" "+re.name + " " + re.score);
            }
        }
    }
}
