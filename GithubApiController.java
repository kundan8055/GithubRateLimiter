/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ratelimiter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author home
 */
public class GithubApiController extends javax.swing.JFrame {

    /**
     * Creates new form GithubApiController
     */
    private ArrayList<User> listOfUsers;
    private final String token = "token 24d3f2ce55f40371d29e00f09f0899b31b232b20";
    private static int remainingCoreCalls = 0;
    private static int remainingSearchCalls = 0;
    
    public GithubApiController() {
        initComponents();    
    }
    
    public void ExtractInfo()
    {
        Thread searchThread = new Thread()
        {
            public void run()
            {
                setRateLimits();
                for(int i = 0; i< listOfUsers.size();i++)
                {
                    User user = listOfUsers.get(i);
                    String userHandle = getUserHandle(user);               
                    
                    JSONObject userProfile = getUserProfile(userHandle);
                    user.userProfile = userProfile;
                    
                    JSONArray userRepository = getUserRepository(userHandle);
                    user.userRepository = userRepository;
                    
                    ArrayList<String> listOfRepos = user.getRepositoryNames();
                    
                    Hashtable commitValues =  getUserCommits(listOfRepos,
                                                        userHandle);
                    user.commitValues = commitValues;
                    
                    listModel.addElement(i+":- "+user.firstName+" "+
                                        user.lastName+" "+ "/"+userHandle);
                        
                }
            }

            
        };
        
        searchThread.start();
    }
    
    public void setRateLimits()
    {
        final String url = "https://api.github.com/rate_limit";
        Request requestRateLimit = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization",token)
                    .build();
        OkHttpClient client = new OkHttpClient();
        Response response;
        try 
        {
            response = client.newCall(requestRateLimit).execute();
            final String responseString = response.body().string();
            JSONObject object = new JSONObject(responseString);
            JSONObject resource = object.getJSONObject("resources");
            JSONObject core = resource.getJSONObject("core");
            JSONObject search = resource.getJSONObject("search");
            remainingCoreCalls = core.getInt("remaining");
            remainingSearchCalls = search.getInt("remaining");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! in retreiving rate limits");
        }
    }
    
    public Hashtable getUserCommits(ArrayList<String> listOfRepos,
                                    String userHandle)
    {
        Hashtable commitValues = new Hashtable();
        OkHttpClient client = new OkHttpClient();
        for(int i = 0;i<listOfRepos.size();i++)
        {
            String repoName = listOfRepos.get(i);
            Request requestNumberOfCommits = new Request.Builder()
                    .url("https://api.github.com/repos/"+userHandle+"/"+
                             repoName+"/commits")
                    .get()
                    .addHeader("Authorization",token)
                    .build();
            Response response;
            try 
            {
                while(remainingCoreCalls==0)
                {
                    setRateLimits();
                }
                response = client.newCall(requestNumberOfCommits).execute();
                final String responseString = response.body().string();
                JSONArray arr = new JSONArray(responseString);
                int numberOfCommits = arr.length();
                commitValues.put(repoName,numberOfCommits);
                remainingCoreCalls--;
            }
            catch(Exception e)
            {
                System.out.println("ERROR!! in retreiving user profile");
            }
        }
        return commitValues;
    }
    
    public JSONObject getUserProfile(String userHandle)
    {
        OkHttpClient client = new OkHttpClient();
        Request requestUserProfile = new Request.Builder()
                    .url("https://api.github.com/users/"+userHandle)
                    .get()
                    .addHeader("Authorization",token)
                    .build();
        Response response;
        JSONObject myObject = null;
        try 
        {
            while(remainingCoreCalls==0)
            {
                setRateLimits();
            }
            response = client.newCall(requestUserProfile).execute();
            final String responseString = response.body().string();
            myObject = new JSONObject(responseString);
            remainingCoreCalls--;
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! in retreiving user profile");
        }
        
        return myObject;
    }
    
    public JSONArray getUserRepository(String userHandle)
    {
        OkHttpClient client = new OkHttpClient();
        Request requestNameOfRepos = new Request.Builder()
                    .url("https://api.github.com/users/"+userHandle+"/repos")
                    .get()
                    .addHeader("Authorization",token)
                    .build();
        Response response;
        JSONArray myArray = null;
        try 
        {
            while(remainingCoreCalls==0)
            {
                setRateLimits();
            }
            response = client.newCall(requestNameOfRepos).execute();
            final String responseString = response.body().string();
            myArray = new JSONArray(responseString);
            remainingCoreCalls--;
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! in retreiving user repos");
        }
        
        return myArray;
    }
    
    public String getUserHandle(User user)
    {
        String url = "https://api.github.com/search/users?q="+
                            user.firstName+"%20in:fullname+%20"+
                            user.lastName+"in:fullname";
                    
        if(user.location.length()!=0)
        {
            url+="location"+user.location;
        }
        
        OkHttpClient client = new OkHttpClient();
        Request requestUserHandle = new Request.Builder()
                                    .url(url)
                                    .get()
                                    .addHeader("Authorization",token)
                                    .build();

        Response response;
        
        String userHandle = "";
        try 
        {
            while(remainingSearchCalls==0)
            {
                setRateLimits();
            }
            response = client.newCall(requestUserHandle).execute();
            final String responseString = response.body().string();
            JSONObject myOb = new JSONObject(responseString);
            JSONArray Arr = myOb.getJSONArray("items");
            JSONObject myObject = Arr.getJSONObject(0);
            userHandle+=myObject.get("login");
            remainingSearchCalls--;
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! in retreiving userhandle");
        }
        return userHandle;
    }
    
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        JList theList = (JList) mouseEvent.getSource();
        if (mouseEvent.getClickCount() == 2) {
          int index = theList.locationToIndex(mouseEvent.getPoint());
          if (index >= 0) {
            System.out.println("Double-clicked on: " + index);
            displayInfo(index);
          }
        }
      }
    };
    
    public void displayInfo(int index)
    {
        User user = listOfUsers.get(index);
        
        String profile = user.firstName+"\n"+user.lastName+"\n"+user.location
                +"\n"+user.userHandle+"\n"+user.getBio()+"\n"+user.getEmailId();
        jTextArea1.setText(profile);
        
        String repoCommitInfo = "";
        ArrayList<String> listOfRepos = user.getRepositoryNames();
        
        for(int i = 0;i<listOfRepos.size();i++)
        {
            String repoName = listOfRepos.get(i);
            String format = "%1$4s\t%2$-50S\t\t%3$-20s";
            String commitValue = ""+user.commitValues.get(repoName);
            String output = String.format(format,i,repoName,commitValue);
            repoCommitInfo+=output+"\n\n";
        }
        jTextArea2.setText(repoCommitInfo);
    }
    
    public GithubApiController(ArrayList<User> listOfUsers)
    {
        initComponents();
        this.listOfUsers = listOfUsers;
        listModel = new DefaultListModel();
        jList2.addMouseListener(mouseListener);
        jList2.setModel(listModel);   
        ExtractInfo();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane3.setViewportView(jTextArea2);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("List Of Users");

        jList2.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(jList2);

        jLabel2.setText("(Select User from List to View Details)");

        jLabel3.setText("(Full Name/ user Handle)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(3, 3, 3)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GithubApiController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GithubApiController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GithubApiController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GithubApiController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GithubApiController().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList<String> jList2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
    DefaultListModel listModel;

}
