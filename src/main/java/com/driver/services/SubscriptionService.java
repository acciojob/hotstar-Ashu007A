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

        try {
            SubscriptionType subscriptionType = subscriptionEntryDto.getSubscriptionType();
            int noOfScreensRequired = subscriptionEntryDto.getNoOfScreensRequired();
            User user = userRepository.findById(subscriptionEntryDto.getUserId()).orElse(null);

            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            int totalAmount = calculateSubscriptionAmount(subscriptionType, noOfScreensRequired);
            Subscription subscription = new Subscription(subscriptionType, noOfScreensRequired, new Date(), totalAmount);
            subscription.setUser(user);
            subscriptionRepository.save(subscription);

            return totalAmount;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Handle the exception appropriately
        }
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository

//        try {
//            User user = userRepository.findById(userId).orElse(null);
//
//            if (user == null) {
//                throw new IllegalArgumentException("User not found");
//            }
//
//            Subscription currentSubscription = user.getSubscription();
//            if (currentSubscription == null) {
//                throw new IllegalStateException("User does not have an existing subscription");
//            }
//
//            SubscriptionType currentSubscriptionType = currentSubscription.getSubscriptionType();
//
//            if (currentSubscriptionType == SubscriptionType.ELITE) {
//                throw new IllegalStateException("Already the best subscription");
//            }
//
//            SubscriptionType nextSubscriptionType = getNextSubscriptionType(currentSubscriptionType);
//
//            int differenceInFare = calculateSubscriptionAmount(nextSubscriptionType, currentSubscription.getNoOfScreensSubscribed())
//                    - currentSubscription.getTotalAmountPaid();
//
//            if (differenceInFare <= 0) {
//                throw new IllegalStateException("Invalid subscription upgrade");
//            }
//
//            Subscription newSubscription = new Subscription(nextSubscriptionType, currentSubscription.getNoOfScreensSubscribed(), new Date(), differenceInFare);
//            newSubscription.setUser(user);
//            subscriptionRepository.save(newSubscription);
//
//            return differenceInFare;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return -1; // Handle the exception appropriately
//        }

        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            Subscription currentSubscription = user.getSubscription();
//            if (currentSubscription == null) {
//                throw new IllegalStateException("User does not have an existing subscription");
//            }

            SubscriptionType currentSubscriptionType = currentSubscription.getSubscriptionType();

            if (currentSubscriptionType == SubscriptionType.ELITE) {
                throw new IllegalStateException("Already the best Subscription");
            }

            SubscriptionType nextSubscriptionType = getNextSubscriptionType(currentSubscriptionType);

            if (nextSubscriptionType == null) {
                throw new IllegalArgumentException("Invalid subscription upgrade");
            }

            int differenceInFare = calculateSubscriptionAmount(nextSubscriptionType, currentSubscription.getNoOfScreensSubscribed())
                    - currentSubscription.getTotalAmountPaid();

            if (differenceInFare <= 0) {
                throw new IllegalArgumentException("Invalid subscription upgrade");
            }

            Subscription newSubscription = new Subscription(nextSubscriptionType, currentSubscription.getNoOfScreensSubscribed(), new Date(), differenceInFare);
            newSubscription.setUser(user);
            subscriptionRepository.save(newSubscription);

            return differenceInFare;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb

        try {
            List<Subscription> subscriptions = subscriptionRepository.findAll();
            int totalRevenue = 0;

            for (Subscription subscription : subscriptions) {
                totalRevenue += subscription.getTotalAmountPaid();
            }

            return totalRevenue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Handle the exception appropriately
        }
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

    private SubscriptionType getNextSubscriptionType(SubscriptionType currentType) {
        switch (currentType) {
            case BASIC:
                return SubscriptionType.PRO;
            case PRO:
                return SubscriptionType.ELITE;
            default:
                return null;
        }
    }
}
