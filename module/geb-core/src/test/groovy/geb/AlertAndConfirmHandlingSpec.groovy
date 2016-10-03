/* Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb

import geb.test.CrossBrowser
import geb.test.GebSpecWithCallbackServer
import geb.test.RequiresRealBrowser
import org.openqa.selenium.NoAlertPresentException

@CrossBrowser
@RequiresRealBrowser
class AlertAndConfirmHandlingSpec extends GebSpecWithCallbackServer {

    def setupSpec() {
        callbackServer.get = { req, res ->
            res.outputStream << """
                <html>
                <body>
                    <script type="text/javascript" charset="utf-8">
                        var i = 0;
                        var confirmResult;
                    </script>
                    <input type="button" name="hasAlert" onclick="alert(++i);" />
                    <input type="button" name="noAlert" />
                    <input type="button" name="hasConfirm" onclick="confirmResult = confirm(++i);" />
                    <input type="button" name="noConfirm" />

                    <input type="button" name="hasAlertReload" onclick="alert(++i); window.location.reload();" />
                    <input type="button" name="noAlertReload" onclick="window.location.reload();"/>
                    <input type="button" name="hasConfirmReload" onclick="confirmResult = confirm(++i); window.location.reload();" />
                    <input type="button" name="noConfirmReload" onclick="window.location.reload();" />

                    <input type="button" name="hasAsynchronousAlert" onclick="setTimeout(function() { alert('asynchronous alert') }, 1000);" />
                    <input type="button" name="hasAsynchronousConfirm" onclick="setTimeout(function() { confirm('asynchronous confirm') }, 1000);" />

                </body>
                </html>
            """
        }
    }

    def setup() {
        go()
    }

    def "handle alert"() {
        expect:
        withAlert { hasAlert().click() } == "1"
    }

    def "expect alert with page change"() {
        expect:
        withAlert { hasAlertReload().click() } == "1"
    }

    def "expect alert but don't get it"() {
        when:
        withAlert { noAlert().click() }

        then:
        thrown(NoAlertPresentException)
    }

    def "expect alert but don't get it with page change"() {
        when:
        withAlert { noAlertReload().click() }

        then:
        thrown(NoAlertPresentException)
    }

    def "nested alerts"() {
        when:
        def innerMsg
        def outerMsg = withAlert {
            innerMsg = withAlert { hasAlert().click() }
            hasAlert().click()
        }
        then:
        innerMsg == "1"
        outerMsg == "2"
    }

    def "withAlert supports waiting"() {
        expect:
        withAlert(wait: true) { hasAsynchronousAlert().click() } == "asynchronous alert"
    }

    private boolean getConfirmResult() {
        js.confirmResult
    }

    def "handle confirm"() {
        expect:
        withConfirm(true) { hasConfirm().click() } == "1"
        confirmResult == true
        withConfirm(false) { hasConfirm().click() } == "2"
        confirmResult == false
        withConfirm { hasConfirm().click() } == "3"
        confirmResult == true
    }

    def "handle confirm with page change"() {
        expect:
        withConfirm(true) { hasConfirmReload().click() } == "1"
    }

    def "expect confirm but don't get it"() {
        when:
        withConfirm { noConfirm().click() }

        then:
        thrown(NoAlertPresentException)
    }

    def "expect confirm but don't get it with page change"() {
        when:
        withConfirm { noConfirmReload().click() }

        then:
        thrown(NoAlertPresentException)
    }

    def "nested confirms"() {
        when:
        def innerMsg
        def innerConfirmResult
        def outerConfirmResult
        def outerMsg = withConfirm(true) {
            innerMsg = withConfirm(false) { hasConfirm().click() }
            innerConfirmResult = confirmResult
            hasConfirm().click()
        }
        outerConfirmResult = confirmResult

        then:
        innerMsg == "1"
        outerMsg == "2"
        innerConfirmResult == false
        outerConfirmResult == true
    }

    def "withConfirm supports waiting"() {
        expect:
        withConfirm(wait: true) { hasAsynchronousConfirm().click() } == "asynchronous confirm"
    }

    def "pages and modules have the methods too"() {
        given:
        page AlertAndConfirmHandlingSpecPage

        when:
        page.testOneOfTheMethods()
        mod.testOneOfTheMethods()

        then:
        notThrown(Exception)
    }
}

class AlertAndConfirmHandlingSpecPage extends Page {
    static content = {
        mod { module AlertAndConfirmHandlingSpecModule }
    }

    def testOneOfTheMethods() {
        withAlert { hasAlert().click() }
    }
}

class AlertAndConfirmHandlingSpecModule extends Module {
    def testOneOfTheMethods() {
        withAlert { hasAlert().click() }
    }
}