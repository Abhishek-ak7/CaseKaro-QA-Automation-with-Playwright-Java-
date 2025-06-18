package com.gocomet.steps;

import com.gocomet.CaseKaroScraper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

public class CaseKaroSteps {
    private CaseKaroScraper scraper;

    @Before
    public void setUp() {
        scraper = new CaseKaroScraper();
    }

    @Given("I am on the CaseKaro website")
    public void i_am_on_the_casekaro_website() {
        scraper.navigateToWebsite();
    }

    @When("I navigate to Mobile Covers section")
    public void i_navigate_to_mobile_covers_section() {
        scraper.navigateToMobileCovers();
    }

    @When("I search for {string} brand")
    public void i_search_for_brand(String brand) {
        scraper.searchForBrand(brand);
    }

    @When("I select Apple brand from the results")
    public void i_select_apple_brand_from_results() {
        scraper.selectAppleBrand();
    }

    @When("I search for {string}")
    public void i_search_for_model(String model) {
        scraper.searchForModel(model);
    }

    @When("I select iPhone 16 Pro from the results")
    public void i_select_iphone_16_pro_from_results() {
        scraper.selectIPhone16Pro();
    }

    @When("I filter for in-stock items")
    public void i_filter_for_in_stock_items() {
        scraper.filterInStockItems();
    }

    @Then("I should be able to extract products from multiple pages")
    public void i_should_be_able_to_extract_products() {
        scraper.extractProducts();
    }

    @Then("products should be sorted by price")
    public void products_should_be_sorted_by_price() {
        scraper.sortAndDisplayProducts();
    }

    @After
    public void tearDown() {
        scraper.cleanup();
    }
}