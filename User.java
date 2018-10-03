/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ratelimiter;

import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONObject;


class User
{
    public String firstName;
    public String lastName;
    public String location;
    public String userHandle;
    public JSONObject userProfile;
    public JSONArray userRepository;
    public Hashtable commitValues;
    
    public User()
    {
    }
    
    public User(String firstName,String lastName,String location)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
    }
    
    public ArrayList<String> getRepositoryNames()
    {
        ArrayList<String> nameOfRepos = new ArrayList<String>();
        try
        {
            for(int i = 0;i<userRepository.length();i++)
            {  
                JSONObject obj = userRepository.getJSONObject(i);
                String name = obj.getString("name");
                nameOfRepos.add(name);
                System.out.println(name);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("ERROR!! while parsing repos array ");
        }
        return nameOfRepos;
    }
    
    public String getEmailId()
    {
        String emailId ="";
        try
        {
            emailId += userProfile.getString("email");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing email from user profile");
        }
        return emailId;
    }
    
    public String getHireableStatus()
    {
        String hireableStatus ="";
        try
        {
            hireableStatus += userProfile.getString("hireable");
        }
        catch(Exception e)
        {
           System.out.println("ERROR!! while parsing hireable from user profile");
        }
        return hireableStatus;
        
    }
    
    public String getBio()
    {
        String profileBio ="";
        try
        {
            profileBio += userProfile.getString("bio");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing bio from user profile");
        }
        return profileBio;
    }
    
    public String getCreatedAt()
    {
        String createdAt="";
        try
        {
            createdAt += userProfile.getString("created_at");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing created from user profile");
        }
        return createdAt;
    }
    
    public String getCompany()
    {
        String company ="";
        try
        {
            company += userProfile.getString("company");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing company from user profile");
        }
        return company;
    }
    
    public int getFollowers()
    {
        int totalFollowers=0;
        try
        {
            totalFollowers += userProfile.getInt("followers");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing followers from user profile");
        }
        return totalFollowers;
    }
    
    public int getFollowing()
    {
        int totalFollowing=0;
        try
        {
            totalFollowing += userProfile.getInt("following");
        }
        catch(Exception e)
        {
            System.out.println("ERROR!! while parsing following from user profile");
        }
        return totalFollowing;
    }
}