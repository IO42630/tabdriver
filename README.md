# TabDriver


* Wrapper for `selenium` to make it easier to use.
* Must use `Chrome` (not `Chromium`).
* Must supply `.properties` file with.

```properties
chrome.driver.path=
headless=false
download.dir=
```

## Usage

### Init

```java
var confPath = Path.of("/foo/tabdriver.properties");
var td = TabDriverBuilder.build(confPath);
```

### Web Navigation

* `TabDriver` is somewhat opionionated.
* The default way of fetching an element is by `css` selector.
    * i.e. `td.findByCss("input[name='q']")`
    * this returns an `Optional<WebElement>` which the consumer can decide how to consume.
    * the optional is empty on any error.


### Quick Recap of CSS Selectors

| Selector                             | Description                                              | 
|--------------------------------------|----------------------------------------------------------|
| `[class*='x']` , `[aria-label*='x']` | `attribute` containing `x`                               | 
| button[class*='email']               | button with a class containing the word `email`          | 
| form[action*='sign']                 | form with an action attribute containing the word `sign` | 
| button.tv-button#email-signin        | button with class `tv-button` and id `email-signin`      | 
