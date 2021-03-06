package hudson.plugins.cigame;

import hudson.model.User;
import hudson.tasks.MailAddressResolver;

import hudson.model.*;
import org.kohsuke.stapler.DataBoundConstructor;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
/**
 * Created by idnvge on 9/26/2014.
 */
public class RockStarUpdater {
    private String ideasRockStarURI = "http://localhost:13082/star";
    private String ideasRockStarEmail= "jenkins.user@ideas.com";

    @DataBoundConstructor
    public RockStarUpdater(String ideasRockStarURI, String ideasRockStarEmail){
        this.ideasRockStarURI=ideasRockStarURI;
        this.ideasRockStarEmail=ideasRockStarEmail;
    }
    public RockStarUpdater(){

    }
    public boolean update(Set<User> players, double score, String reason, BuildListener listener, String badge) throws IOException, ClassNotFoundException {

    	listener.getLogger().append("[ci-game] about to post Rock Star status\n");
        for(User player:players) {
            try {
                UserScoreProperty property = player.getProperty(UserScoreProperty.class);
                listener.getLogger().append("[ci-game] finding email address for player: " + player + "... ");
                String emailId = MailAddressResolver.resolve(player);
                listener.getLogger().append(emailId + "\n");

	            URL obj = new URL(ideasRockStarURI+"/trophy/save");
	            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	    		listener.getLogger().append("[ci-game] posting to URL: " + ideasRockStarURI + "\n");

	            con.setRequestMethod("POST");
	            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	            con.setRequestProperty("Content-Type", "application/json");

	            String urlParameters = "{ \"fromUserEmailID\":\""+ideasRockStarEmail+"\", \"toUserEmailID\":\"" +
	                    emailId+"\"" +
	                    ",\"trophies\":" +
	                    score +
                        ",\"badgeName\":" +
                        "\""+badge +"\""+
	                    ",\"reason\":\"Jenkins:"+reason+"\"}";

	            con.setRequestProperty("Content-Length", "" + urlParameters.length());

	            System.out.println("\nSending 'POST' request to URL : " + ideasRockStarURI);
	            System.out.println("Post parameters : " + urlParameters);
	            listener.getLogger().append("[ci-game] Post parameters: " + urlParameters + "\n");

	            // Send post request
	            con.setDoOutput(true);
	            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	            wr.writeBytes(urlParameters);
	            wr.flush();
	            wr.close();

	            int responseCode = con.getResponseCode();


	            listener.getLogger().append("[ci-game] Response Code : " + responseCode+"\n");

	            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();
            }catch (Exception e) {
            	listener.getLogger().append("[ci-game] Could not post stats for player: " + player+"\n");
            }
        }
        return true;
    }
}
