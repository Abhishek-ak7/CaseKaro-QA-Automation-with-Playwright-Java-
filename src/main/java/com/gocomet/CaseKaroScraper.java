package com.gocomet;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import java.util.*;

public class CaseKaroScraper {
    static class Product {
        String description;
        String actualPrice;
        String discountedPrice;
        String imageLink;

        public Product(String description, String actualPrice, String discountedPrice, String imageLink) {
            this.description = description;
            this.actualPrice = actualPrice;
            this.discountedPrice = discountedPrice;
            this.imageLink = imageLink;
        }

        @Override
        public String toString() {
            return String.format("Description: %s\nActual Price: %s\nDiscounted Price: %s\nImage Link: %s",
                    description, actualPrice, discountedPrice, imageLink);
        }
    }

    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // Step 1: Navigate to website
        System.out.println("Step 1: Navigating to CaseKaro website...");
        page.navigate("https://casekaro.com/");
        assert page.title().contains("CaseKaro") : "Homepage did not load successfully";

        // Step 2: Click on Mobile Covers
        System.out.println("Step 2: Clicking on Mobile Covers...");
        Locator mobileCovers = page.locator("#HeaderMenu-mobile-covers");
        mobileCovers.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mobileCovers.click();
        page.waitForLoadState();
        assert page.url().contains("mobile-covers") : "Mobile covers page not loaded";

        // Step 3: Search for Apple
        System.out.println("Step 3: Searching for Apple...");
        Locator searchBox = page.locator("input#search-bar-cover-page");
        searchBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchBox.click();
        searchBox.fill("Apple");
        page.waitForTimeout(2000);
        
        // Step 4: Brand validations
        System.out.println("Step 4: Validating brand filtering...");
        assert page.locator("text=Samsung").isHidden() : "Samsung brand is visible";
        assert page.locator("text=OnePlus").isHidden() : "OnePlus brand is visible";
        assert page.locator("text=Google").isHidden() : "Google brand is visible";
        assert page.locator(".brand-name-container:has-text('Apple')").isVisible() : "Apple option not visible";

        // Step 5: Click Apple and handle new tab
        System.out.println("Step 5: Clicking on Apple brand...");
        final Page currentPage = page; // Make page effectively final
        Page newPage = context.waitForPage(() -> {
            currentPage.locator(".brand-name-container:has-text('Apple')").click();
        });
        
        page = newPage;
        page.waitForLoadState();
        assert page.url().contains("apple") : "Apple page not loaded";
        
        // Step 6: Search and select iPhone 16 Pro
        System.out.println("Step 6: Searching for iPhone 16 Pro...");
        searchBox = page.locator("input#search-bar-cover-page");
        searchBox.waitFor();
        searchBox.click();
        searchBox.fill("iPhone 16 Pro");
        page.waitForTimeout(3000);
        
        Locator iphone16Pro = page.locator(".brand-name-container").filter(new Locator.FilterOptions().setHasText("iPhone 16 Pro")).first();
        assert iphone16Pro.isVisible() : "iPhone 16 Pro option not visible";
        iphone16Pro.click();
        page.waitForLoadState();
        assert page.url().contains("iphone-16-pro") : "iPhone 16 Pro page not loaded";

        // Step 7: Apply availability filter
        System.out.println("Step 7: Applying availability filter...");
        Locator availabilityDropdown = page.locator("#Details-filter\\.v\\.availability-template--16941146603638__product-grid summary");
        availabilityDropdown.waitFor();
        availabilityDropdown.click();
        
        Locator inStockLabel = page.locator("label.facet-checkbox:has-text('In stock')").first();
        inStockLabel.waitFor();
        inStockLabel.click();
        
        page.waitForLoadState();
        page.waitForTimeout(2000);
        assert page.locator(".facets__selected").isVisible() : "Filter not applied";

        // Step 8: Extract products from both pages
        System.out.println("Step 8: Extracting products from multiple pages...");
        List<Product> products = new ArrayList<>();

        // Scrape first page
        System.out.println("\nScraping page 1...");
        page.waitForSelector("#product-grid");
        scrapeCurrentPage(page, products);

        // Navigate to page 2
        System.out.println("\nNavigating to page 2...");
        page.waitForSelector("nav.pagination");

        // Click page 2 using JavaScript
        page.evaluate("document.querySelector('a.pagination__item[aria-label=\"Page 2\"]').click()");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Verify navigation
        assert page.url().contains("page=2") : "Failed to navigate to page 2";
        
        // Scrape page 2
        page.waitForSelector("#product-grid");
        System.out.println("Scraping page 2...");
        scrapeCurrentPage(page, products);

        // Step 9: Sort and display results
        System.out.println("\nStep 9: Sorting products by price...");
        products.sort((p1, p2) -> Double.compare(
            extractNumericPrice(p1.discountedPrice), 
            extractNumericPrice(p2.discountedPrice)
        ));

        // Print results
        System.out.println("\n=== Products sorted by discounted price (ascending) ===");
        System.out.println("Total products: " + products.size());
        products.forEach(product -> {
            System.out.println("\n-------------------");
            System.out.println(product);
        });

        // Close resources
        browser.close();
        playwright.close();
    }

    private static void scrapeCurrentPage(Page page, List<Product> products) {
        List<ElementHandle> productItems = page.querySelectorAll("#product-grid li.grid__item");
        System.out.println("Found " + productItems.size() + " products on current page");

        for (ElementHandle item : productItems) {
            String description = item.querySelector(".card__heading a").textContent().trim();
            String actualPrice = item.querySelector(".price__sale .price-item--regular").textContent()
                    .replace("₹", "").trim();
            String discountedPrice = item.querySelector(".price__sale .price-item--sale").textContent()
                    .replace("From ₹", "").trim();
            String imageLink = item.querySelector(".card__media img").getAttribute("src");
            
            if (imageLink.contains("?")) {
                imageLink = imageLink.substring(0, imageLink.indexOf("?"));
            }
            
            products.add(new Product(description, actualPrice, discountedPrice, imageLink));
        }
    }

    private static double extractNumericPrice(String priceString) {
        return Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
    }
}