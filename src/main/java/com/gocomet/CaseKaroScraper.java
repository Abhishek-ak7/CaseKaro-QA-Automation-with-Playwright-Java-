package com.gocomet;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import java.util.*;

public class CaseKaroScraper {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private List<Product> products = new ArrayList<>();

    static class Product {
        String description;
        String actualPrice;
        String discountedPrice;
        String imageLink;

        public Product(String description, String actualPrice, String discountedPrice, String imageLink) {
            this.description = description.trim();
            this.actualPrice = actualPrice.trim();
            this.discountedPrice = discountedPrice.trim();
            this.imageLink = imageLink.trim();
        }

        @Override
        public String toString() {
            String priceDisplay = actualPrice.equals(discountedPrice) ? 
                String.format("Price: %s", actualPrice) :
                String.format("Original Price: %s (Discounted: %s)", actualPrice, discountedPrice);

            return String.format("""
                +----------------------------------------------------------------------------------------+
                | Product Details:
                | Description: %-70s
                | %s
                | Image Link: %-75s
                """,
                description,
                priceDisplay,
                "https:" + imageLink);
        }
    }

    public void navigateToWebsite() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(false)
            .setTimeout(30000));
        context = browser.newContext();
        page = context.newPage();
        
        page.navigate("https://casekaro.com/");
        
        // Wait for page load
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        
        // More flexible title check that accepts various forms of the website title
        String title = page.title();
        if (!(title.contains("Casekaro") || title.contains("Case Karo") || 
              title.contains("Phone Back Cover"))) {
            throw new AssertionError("Homepage did not load successfully. Current title: " + title);
        }
    }

    public void navigateToMobileCovers() {
        page.waitForSelector("#HeaderMenu-mobile-covers", 
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

        page.click("#HeaderMenu-mobile-covers");

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(2000);

        String currentUrl = page.url().toLowerCase();
        if (!currentUrl.contains("mobile-covers") && 
            !currentUrl.contains("phone-cases") &&
            !currentUrl.contains("mobile-back-cover-case")) {
            throw new AssertionError("Mobile covers page not loaded. Current URL: " + currentUrl);
        }
    }

    public void searchForBrand(String brand) {
        Locator searchBox = page.locator("input#search-bar-cover-page");
        searchBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchBox.click();
        searchBox.fill(brand);
        page.waitForTimeout(2000);
    }

    public void selectAppleBrand() {
        final Page currentPage = page;
        Page newPage = context.waitForPage(() -> {
            currentPage.locator(".brand-name-container:has-text('Apple')").click();
        });
        page = newPage;
        page.waitForLoadState();
    }

    public void searchForModel(String model) {
        Locator searchBox = page.locator("input#search-bar-cover-page");
        searchBox.waitFor();
        searchBox.click();
        searchBox.fill(model);
        page.waitForTimeout(3000);
    }

    public void selectIPhone16Pro() {
        Locator iphone16Pro = page.locator(".brand-name-container").filter(new Locator.FilterOptions().setHasText("iPhone 16 Pro")).first();
        iphone16Pro.click();
        page.waitForLoadState();
    }

    public void filterInStockItems() {
        Locator availabilityDropdown = page.locator("#Details-filter\\.v\\.availability-template--16941146603638__product-grid summary");
        availabilityDropdown.waitFor();
        availabilityDropdown.click();
        
        Locator inStockLabel = page.locator("label.facet-checkbox:has-text('In stock')").first();
        inStockLabel.waitFor();
        inStockLabel.click();
        
        page.waitForLoadState();
        page.waitForTimeout(2000);
    }

    public void extractProducts() {
        System.out.println("Current URL: " + page.url());

        System.out.println("Scraping page 1...");
        scrapeCurrentPage();
        System.out.println("Page 1 products: " + products.size());

        String currentUrl = page.url();
        String page2Url = currentUrl + "?page=2";

        System.out.println("Navigating to page 2: " + page2Url);
        page.navigate(page2Url);

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(2000);

        System.out.println("Scraping page 2...");
        scrapeCurrentPage();
        System.out.println("Total products after page 2: " + products.size());
    }

    private void scrapeCurrentPage() {
        page.waitForSelector(".grid.product-grid", 
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30000));

        List<ElementHandle> productCards = page.querySelectorAll("li.grid__item");
        System.out.println("Found " + productCards.size() + " products on current page");

        for (ElementHandle card : productCards) {
            String description = "";
            ElementHandle titleElement = card.querySelector(".full-unstyled-link");
            if (titleElement != null) {
                description = titleElement.innerText();
            }

            String actualPrice = "N/A";
            ElementHandle regularPriceElement = card.querySelector(".price__regular .price-item--regular");
            if (regularPriceElement != null) {
                actualPrice = regularPriceElement.innerText();
            }

            String discountedPrice = actualPrice;
            ElementHandle salePriceElement = card.querySelector(".price__sale .price-item--sale");
            if (salePriceElement != null) {
                discountedPrice = salePriceElement.innerText();
                ElementHandle originalPriceElement = card.querySelector(".price__sale .price-item--regular");
                if (originalPriceElement != null) {
                    actualPrice = originalPriceElement.innerText();
                }
            }

            String imageLink = "";
            ElementHandle img = card.querySelector(".card__media img");
            if (img != null) {
                imageLink = img.getAttribute("src");
            }

            if (!description.isEmpty()) {
                products.add(new Product(
                    description.trim(),
                    actualPrice.replace("Regular price", "").trim(),
                    discountedPrice.trim(),
                    imageLink.trim()
                ));
                System.out.println("Added product: " + description);
            }
        }
    }

    public void sortAndDisplayProducts() {
        // Sort products by price
        products.sort((p1, p2) -> Double.compare(
            extractNumericPrice(p1.discountedPrice), 
            extractNumericPrice(p2.discountedPrice)
        ));

        // Print results in a formatted way
        System.out.println("\n============== Products Sorted By Price (Low to High) ==============");
        System.out.println("Total Products Found: " + products.size());
        System.out.println("====================================================================\n");
        
        products.forEach(System.out::println);
    }

    public void cleanup() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private void scrapeCurrentPage(Page page, List<Product> products) {
        // Wait for product grid
        Locator productGrid = page.locator(".grid.product-grid");
        productGrid.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assert productGrid.isVisible() : "Product grid not visible";

        // Get all product cards
        List<ElementHandle> productCards = page.querySelectorAll("li.grid__item");
        assert !productCards.isEmpty() : "No product cards found";
        System.out.println("Found " + productCards.size() + " products on current page");

        for (ElementHandle card : productCards) {
            ElementHandle titleElement = card.querySelector(".full-unstyled-link");
            assert titleElement != null : "Product title element not found";
            String description = titleElement.innerText();

            ElementHandle priceContainer = card.querySelector(".price__container");
            assert priceContainer != null : "Price container not found";

            String actualPrice = "N/A";
            ElementHandle regularPriceElement = priceContainer.querySelector(".price__regular .price-item--regular");
            if (regularPriceElement != null) {
                actualPrice = regularPriceElement.innerText();
            }

            String discountedPrice;
            ElementHandle salePriceElement = priceContainer.querySelector(".price__sale .price-item--sale");
            if (salePriceElement != null) {
                discountedPrice = salePriceElement.innerText();
                ElementHandle originalPriceElement = priceContainer.querySelector(".price__sale .price-item--regular");
                if (originalPriceElement != null) {
                    actualPrice = originalPriceElement.innerText();
                }
            } else {
                discountedPrice = actualPrice;
            }

            ElementHandle img = card.querySelector(".card__media img");
            assert img != null : "Product image not found";
            String imageLink = img.getAttribute("src");

            assert !description.isEmpty() : "Product description is empty";
            assert !actualPrice.equals("N/A") : "Product price not found";
            assert imageLink != null && !imageLink.isEmpty() : "Product image link is empty";

            products.add(new Product(
                description.trim(),
                actualPrice.replace("Regular price", "").trim(),
                discountedPrice.trim(),
                imageLink.trim()
            ));
        }
    }

    private static double extractNumericPrice(String priceString) {
        return Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
    }
}