package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){

        //Save The subscription Object into the Db and return the total Amount that user has to pay

        SubscriptionType subscriptionType = subscriptionEntryDto.getSubscriptionType();
        int noOfScreensRequired = subscriptionEntryDto.getNoOfScreensRequired();

        int totalAmount = calculateSubscriptionAmount(subscriptionType, noOfScreensRequired);

        Subscription subscription = new Subscription(
                subscriptionType,
                noOfScreensRequired,
                new Date(),  // Set the subscription start date to the current date
                totalAmount
        );

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return savedSubscription.getTotalAmountPaid();
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new Exception("User not found");
        }

        SubscriptionType currentSubscriptionType = user.getSubscription().getSubscriptionType();

        if (currentSubscriptionType == SubscriptionType.ELITE) {
            throw new Exception("Already the best Subscription");
        }

        int fareDifference = calculateFareDifference(currentSubscriptionType);

        SubscriptionType newSubscriptionType = getNextSubscriptionType(currentSubscriptionType);
        user.getSubscription().setSubscriptionType(newSubscriptionType);

        userRepository.save(user);

        return fareDifference;
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb

        int totalRevenue = subscriptionRepository.findAll().stream()
                .mapToInt(Subscription::getTotalAmountPaid)
                .sum();

        return totalRevenue;
    }

    private int calculateSubscriptionAmount(SubscriptionType subscriptionType, int noOfScreensRequired) {
        int baseAmount = 0;
        int screenRate = 0;

        switch (subscriptionType) {
            case BASIC:
                baseAmount = 500;
                screenRate = 200;
                break;
            case PRO:
                baseAmount = 800;
                screenRate = 250;
                break;
            case ELITE:
                baseAmount = 1000;
                screenRate = 350;
                break;
        }

        return baseAmount + screenRate * noOfScreensRequired;
    }

    private int calculateFareDifference(SubscriptionType currentSubscriptionType) {
        int currentFare = 0;
        int nextFare = 0;

        switch (currentSubscriptionType) {
            case BASIC:
                currentFare = 500;
                nextFare = 800;
                break;
            case PRO:
                currentFare = 800;
                nextFare = 1000;
                break;
        }

        return nextFare - currentFare;
    }

    private SubscriptionType getNextSubscriptionType(SubscriptionType currentSubscriptionType) {
        switch (currentSubscriptionType) {
            case BASIC:
                return SubscriptionType.PRO;
            case PRO:
                return SubscriptionType.ELITE;
            default:
                return currentSubscriptionType;
        }
    }
}
