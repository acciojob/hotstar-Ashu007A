package com.driver.services;

import com.driver.EntryDto.WebSeriesEntryDto;
import com.driver.model.ProductionHouse;
import com.driver.model.WebSeries;
import com.driver.repository.ProductionHouseRepository;
import com.driver.repository.WebSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebSeriesService {

    @Autowired
    WebSeriesRepository webSeriesRepository;

    @Autowired
    ProductionHouseRepository productionHouseRepository;

    public Integer addWebSeries(WebSeriesEntryDto webSeriesEntryDto)throws  Exception{

        //Add a webSeries to the database and update the ratings of the productionHouse
        //Incase the seriesName is already present in the Db throw Exception("Series is already present")
        //use function written in Repository Layer for the same
        //Dont forget to save the production and webseries Repo

        String seriesName = webSeriesEntryDto.getSeriesName();
        if (webSeriesRepository.findBySeriesName(seriesName) != null) {
            throw new Exception("Series is already present");
        }

        // Retrieve the production house based on the provided production house ID
        Integer productionHouseId = webSeriesEntryDto.getProductionHouseId();
        ProductionHouse productionHouse = productionHouseRepository.findById(productionHouseId)
                .orElseThrow(() -> new Exception("Production house not found"));

        // Create a new web series object
        WebSeries webSeries = new WebSeries(
                seriesName,
                webSeriesEntryDto.getAgeLimit(),
                webSeriesEntryDto.getRating(),
                webSeriesEntryDto.getSubscriptionType()
        );

        // Set the production house for the web series
        webSeries.setProductionHouse(productionHouse);

        // Save the web series and update the ratings of the production house
        WebSeries savedWebSeries = webSeriesRepository.save(webSeries);

        // Calculate the new ratings for the production house
        double productionHouseRatings = calculateProductionHouseRatings(productionHouse);

        // Update the production house ratings
        productionHouse.setRatings(productionHouseRatings);
        productionHouseRepository.save(productionHouse);

        return savedWebSeries.getId();
    }

    private double calculateProductionHouseRatings(ProductionHouse productionHouse) {

        double totalRatings = productionHouse.getWebSeriesList().stream()
                .mapToDouble(WebSeries::getRating)
                .sum();

        int numberOfWebSeries = productionHouse.getWebSeriesList().size();
        if (numberOfWebSeries > 0) {
            return totalRatings / numberOfWebSeries;
        } else {
            return 0.0;
        }
    }

}
