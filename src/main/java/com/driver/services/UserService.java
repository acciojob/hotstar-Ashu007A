package com.driver.services;


import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.model.WebSeries;
import com.driver.repository.UserRepository;
import com.driver.repository.WebSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebSeriesRepository webSeriesRepository;


    public Integer addUser(User user){

        //Jut simply add the user to the Db and return the userId returned by the repository

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    public Integer getAvailableCountOfWebSeriesViewable(Integer userId){

        //Return the count of all webSeries that a user can watch based on his ageLimit and subscriptionType
        //Hint: Take out all the Webseries from the WebRepository

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return -1; // User not found
        }

        SubscriptionType userSubscriptionType = user.getSubscription().getSubscriptionType();
        int userAge = user.getAge();

        List<WebSeries> allWebSeries = webSeriesRepository.findAll();

        long count = allWebSeries.stream()
                .filter(webSeries -> isWebSeriesViewable(webSeries, userSubscriptionType, userAge))
                .count();

        return (int) count;
    }

    private boolean isWebSeriesViewable(WebSeries webSeries, SubscriptionType userSubscriptionType, int userAge) {

        SubscriptionType webSeriesSubscriptionType = webSeries.getSubscriptionType();
        int webSeriesAgeLimit = webSeries.getAgeLimit();

        if (userSubscriptionType == SubscriptionType.ELITE) {
            return true;
        } else if (userSubscriptionType == SubscriptionType.PRO && webSeriesSubscriptionType != SubscriptionType.BASIC) {
            return true;
        } else if (userSubscriptionType == SubscriptionType.BASIC && webSeriesSubscriptionType == SubscriptionType.BASIC) {
            return true;
        }

        return userAge >= webSeriesAgeLimit;
    }
}
