package com.veeru;

public class Repository {
    String orgName;
    String noOfRepo;
    String noOfContr;

    public String getOrgName() {
        return orgName;
    }

    public String getNoOfRepo() {
        return noOfRepo;
    }

    public String getNoOfContr() {
        return noOfContr;
    }

    public void setOrgName(String userName) {
        this.orgName = userName;
    }

    public void setNoOfRepo(String noOfRepo) {
        this.noOfRepo = noOfRepo;
    }

    public void setNoOfContr(String noOfContr) {
        this.noOfContr = noOfContr;
    }
}
