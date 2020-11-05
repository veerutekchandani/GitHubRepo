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
    private int pageNo;

    /* mapping for home page */
    @RequestMapping("/")
    public ModelAndView home(Model model) throws IOException {
        return new ModelAndView("home", "repo", new Repository());
    }

    /* mapping for showing repo on form submit*/
    @RequestMapping("/submit")
    public String submitForm(@ModelAttribute("repo") Repository repository, ModelMap modelMap) {
        if (repository.getNoOfRepo() == null) // if reload happens and there is no value set
            return "home";

        int n = Integer.valueOf(repository.getNoOfRepo());
        int m = Integer.valueOf(repository.getNoOfContr());
        String orgName = repository.getOrgName();

        HashMap<String, Integer> repos = findRepo(orgName, n, m); // calling findRepo for all top n repositories
        Map<String, HashMap<String, Integer>> result = findContributors(orgName, n, m, repos); // top m contributors
        if (result.isEmpty()) { // if result is empty then error is shown
            modelMap.addAttribute("error", "No repositories found\r\n1.either limit reached\r\n2.username not found");
        } else {
            Iterator iterator = result.entrySet().iterator();
            modelMap.addAttribute("orgName", orgName); // setting org Name for home.jsp
            modelMap.addAttribute("repoList", result);
        }
        return "home";
    }

    /* function to  get top n repositories*/
    public HashMap<String, Integer> findRepo(String userName, int n, int m) {
        try {
            pageNo = 1;
            String URL = "https://api.github.com/users/" + userName + "/repos?page="; // hitting github api
            HashMap<String, Integer> result = new HashMap<>();
            while (true) { // running loop for all pages as limit of api is 100 repos
                url = new URL(URL + pageNo);
                System.out.println(URL + pageNo);
                httpURLConnection = (HttpURLConnection) url.openConnection(); // opening connection
                HashMap<String, Integer> topRepos = getResult(httpURLConnection, 1); // calling function to get result from http request
                if (topRepos == null || topRepos.isEmpty())
                    break;
                result.putAll(topRepos);
                pageNo++;
                httpURLConnection.disconnect();
            }
            result = sortByValueDescending(result); // sort by no. of fork count in descending order

            // insert top n repos into hasmap
            HashMap<String, Integer> finalResult = new HashMap<>();
            int counter = 1;
            Iterator iterator = result.entrySet().iterator();
            while (counter <= n && iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                finalResult.put(entry.getKey(), entry.getValue());
                counter++;
            }
            return finalResult;
        } catch (Exception e) {

        }
        return null;
    }

    /*
        function to find top m contributors
        orgName : organization name
        n : no. of repositories
        m : no. of contributors
        repos : top n repositories
    */
    public HashMap<String, HashMap<String, Integer>> findContributors(String userName, int n, int m, HashMap<String, Integer> repos) {
        try {
            Iterator iterator = repos.entrySet().iterator();
            HashMap<String, HashMap<String, Integer>> result = new HashMap<>();
            HashMap<String, Integer> contributors = new HashMap<>();
            while (iterator.hasNext()) { // iterate over all repositories to find top contributors
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                pageNo = 1;
                while (true) { // iterate over all the pages of contributors
                    String URL = "https://api.github.com/repos/" + userName + "/" + entry.getKey() + "/contributors?page=";
                    url = new URL(URL + pageNo);
                    System.out.println(URL + pageNo);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    HashMap<String, Integer> topContributors = getResult(httpURLConnection, 2);
                    if (topContributors == null || topContributors.isEmpty()) {
                        break;
                    }
                    contributors.putAll(topContributors);
                    pageNo++;
                    httpURLConnection.disconnect();
                }
                contributors = sortByValueDescending(contributors); // sort by no. of contributions in descending order
                System.out.println("came here");
                // insert top m contributors into hashmap
                Iterator iterator1 = contributors.entrySet().iterator();
                int counter = 1;
                HashMap<String, Integer> contr = new HashMap<>();
                while (counter <= m && iterator1.hasNext()) {
                    Map.Entry<String, Integer> entry1 = (Map.Entry<String, Integer>) iterator1.next();
                    if (!(entry1.getKey()).equalsIgnoreCase("dependabot[bot]"))
                        contr.put(entry1.getKey(), entry1.getValue());
                    System.out.println(entry1.getKey());
                    counter++;
                }
                contr = sortByValueDescending(contr);
                result.put(entry.getKey(), contr);
            }
            return result;
        } catch (Exception e) {
            System.out.println("Exception occured");
        }
        return null;
    }

    /*
        function to get result for http request
        index : 1 for repo, 2 for contr
     */
    public HashMap<String, Integer> getResult(HttpURLConnection httpURLConnection, int index) {
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
            if (jsonArray.isEmpty())
                return null;

            Iterator iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                JSONObject jsonObject = new JSONObject(iterator.next().toString());
                if (index == 1)
                    hashMap.put((String) jsonObject.get("name"), (int) jsonObject.get("forks_count"));
                else {
                    hashMap.put((String) jsonObject.get("login"), (int) jsonObject.get("contributions"));
                }
            }
            return hashMap;
        } catch (Exception e) {

        }
        return null;
    }

    // function to sort hashmap based on key in descending order
    public HashMap<String, Integer> sortByValueDescending(HashMap<String, Integer> hashMap) {
        return hashMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
}
