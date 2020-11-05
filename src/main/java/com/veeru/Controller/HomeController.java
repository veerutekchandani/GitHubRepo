package com.veeru.Controller;

import com.veeru.Repository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@Controller
public class HomeController {

    private URL url;
    private HttpURLConnection httpURLConnection;
    int page;

    @RequestMapping("/home")
    public ModelAndView home(Model model) throws IOException {
        return new ModelAndView("home", "repo", new Repository());
    }

    @RequestMapping("/submit")
    public String submitForm(@ModelAttribute("repo")Repository repository, ModelMap modelMap) {
        if(repository.getN() == null)
            return "home";
        int n = Integer.valueOf(repository.getN());
        int m = Integer.valueOf(repository.getM());
        modelMap.addAttribute("userName",repository.getUserName());
        modelMap.addAttribute("n",n);
        modelMap.addAttribute("m",m);
        HashMap<String,Integer> repos = findRepo(repository.getUserName(),n,m);
        Map<String,ArrayList<String>> result =
                findContributors(repository.getUserName(),n,m,repos);
        if(result.isEmpty()) {
            modelMap.addAttribute("error","No repositories found\r\n1.either limit reached\r\n2.username not found");
        }
        else {
            modelMap.addAttribute("repoList", result);
        }
        return "home";
    }

    public HashMap<String,Integer> findRepo(String userName,int n,int m) {
        try {
            page = 1;
            String URL = "https://api.github.com/users/"+userName+"/repos?per_page=100&page=";
            HashMap<String,Integer> result = new HashMap<>();
            while(true) {
                url = new URL(URL + page);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                HashMap<String, Integer> topRepos = getResult(httpURLConnection,1);
                if(topRepos==null)
                    break;
                result.putAll(topRepos);
                page++;
                httpURLConnection.disconnect();
            }
            result = result.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            HashMap<String,Integer> finalResult = new HashMap<>();
            int counter = 1;
            Iterator iterator = result.entrySet().iterator();
            while(counter<=n && iterator.hasNext()) {
                Map.Entry<String,Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                finalResult.put(entry.getKey(),entry.getValue());
                counter++;
            }
            return finalResult;
        }
        catch (Exception e) {

        }
        return null;
    }

    public Map<String,ArrayList<String>> findContributors(String userName,int n,int m,HashMap<String,Integer> repos) {
        try {
            Iterator iterator = repos.entrySet().iterator();
            Map<String,ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
            while(iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                ArrayList<String> contr = new ArrayList<>();
                page=1;
                while(true) {
                    String URL = "https://api.github.com/repos/" + userName + "/" + entry.getKey() + "/contributors?per_page=100&page=";
                    url = new URL(URL + page);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    HashMap<String, Integer> topContributors = getResult(httpURLConnection,2);
                    if(topContributors==null) {
                        break;
                    }
                    Iterator iterator1 = topContributors.entrySet().iterator();

                    int counter = 1;
                    while (counter <= m && iterator1.hasNext()) {
                        Map.Entry<String,Integer> entry1 = (Map.Entry<String, Integer>) iterator1.next();
                        contr.add(entry1.getKey());
                    }
                    page++;
                }
                result.put(entry.getKey(),contr);
                httpURLConnection.disconnect();
            }
            return result;
        }
        catch (Exception e) {

        }
        return null;
    }

    public HashMap<String,Integer> getResult(HttpURLConnection httpURLConnection,int index) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(new BufferedInputStream(httpURLConnection.getInputStream())));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONArray jsonArray = new JSONArray(result.toString());
            if(jsonArray.isEmpty())
                return null;

            Iterator iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                JSONObject jsonObject = new JSONObject(iterator.next().toString());
                if(index==1)
                    hashMap.put((String) jsonObject.get("name"), (int) jsonObject.get("forks_count"));
                else {
                    hashMap.put((String) jsonObject.get("login"), (int) jsonObject.get("contributions"));
                }
            }

            return hashMap;
        }
        catch (Exception e) {

        }
        return null;
    }
}
