# 🧪 CaseKaro QA Automation with Playwright & Java

This project is a robust **end-to-end automation script** developed for QA testing of [CaseKaro](https://casekaro.com), an e-commerce platform. It uses **Playwright with Java** to simulate user interactions and perform validations on product filters, search, and data extraction functionalities.

---

## 🔍 Project Objective

To demonstrate **automated UI testing and data validation** in a real-world QA scenario by:

- Navigating and interacting with a live e-commerce site
- Applying filters (brand, availability)
- Validating search accuracy with **negative brand checks**
- Extracting and sorting product information programmatically
- Demonstrating Playwright’s power for **modern QA automation roles**

---

## 🚀 Tech Stack

- **Java** – Core programming language
- **Playwright for Java** – UI Automation & Testing
- **Maven** – Dependency Management & Build
- **GitHub** – Version Control
- **VS Code / IntelliJ** – Recommended IDEs

---

## 🧠 Key Features

✔️ Navigate to [CaseKaro](https://casekaro.com)  
✔️ Click "Mobile Covers" and search for **Apple**  
✔️ Perform **negative validation** for unrelated brands  
✔️ Select **Apple** brand and filter by **In Stock** availability  
✔️ Search and click on **iPhone 16 Pro**  
✔️ Scrape product details:
- 🏷 Description  
- 💲 Actual Price  
- 🤑 Discounted Price  
- 🖼 Image URL  
✔️ Navigate and extract from **2 pages**  
✔️ Sort products by **discounted price (ascending)**  
✔️ Output product data in clean format

---
##  Cucumber Integration
Write your BDD scenarios in .feature files:

> ```text
Feature: CaseKaro Product Search and Filter

  Scenario: Search for iPhone 16 Pro cases and verify pricing
    Given I am on the CaseKaro website
    When I navigate to Mobile Covers section
    And I search for "Apple" brand
    And I select Apple brand from the results
    And I search for "iPhone 16 Pro"
    And I select iPhone 16 Pro from the results
    And I filter for in-stock items
    Then I should be able to extract products from multiple pages
    And products should be sorted by price

>```


## 🚀 Run Automation Tests

Run the test cases using the following Maven command:

>
> ```bash
> mvn clean test
> ```

## 📸 Sample Output

```text
+----------------------------------------------------------------------------------------+
| Product Details:
| Description: Satyamev Jayate Golden iPhone 16 Pro Glass Case
| Original Price: ? 699.00 (Discounted: ? 249.00)        
| Image Link: https://casekaro.com/cdn/shop/files/GC-0623-iP16PRO-0580-A_e1c98492-65ba-4262-8a4c-82e6558a702b.jpg?v=1729762672&width=533
+----------------------------------------------------------------------------------------+

+----------------------------------------------------------------------------------------+
| Product Details:
| Description: Satyamev Jayate Golden iPhone 16 Pro Glass Case
| Original Price: ? 699.00 (Discounted: ? 249.00)        
| Image Link: https://casekaro.com/cdn/shop/files/GC-0623-iP16PRO-0580-A_e1c98492-65ba-4262-8a4c-82e6558a702b.jpg?v=1729762672&width=533
+----------------------------------------------------------------------------------------+
'
