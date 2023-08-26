# TabDriver

* Wrapper for `selenium` to make it easier to use.
* Must use `Chrome` (not `Chromium`).
* Must supply `.properties` file with.


    chrome.driver.path=
    headless=false
    download.dir=

* Usage:


    var confPath = Path.of("/foo/tabdriver.properties");
	var td = TabDriverBuilder.build(confPath);