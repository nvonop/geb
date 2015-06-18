/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javascript

import geb.test.GebSpecWithCallbackServer

class AlertSpec extends GebSpecWithCallbackServer {

    def "alert"() {
        given:
        html """
            <html>
                // tag::alert_html[]
                <input type="button" name="showAlert" onclick="alert('Bang!');" />
                // end::alert_html[]
            </html>
        """

        expect:
        // tag::alert[]
        assert withAlert { $("input", name: "showAlert").click() } == "Bang!"
        // end::alert[]
    }

    def "async alert"() {
        given:
        html {
            input(type: "button", name: "showAlert", onclick: "setTimeout(function() { alert('Bang!'); }, 100);")
        }

        expect:
        // tag::async_alert[]
        assert withAlert(wait: true) { $("input", name: "showAlert").click() } == "Bang!"
        // end::async_alert[]
    }

    def "no alert"() {
        given:
        html """
            <html>
                // tag::no_alert_html[]
                <input type="button" name="contShowAlert" />
                // end::no_alert_html[]
            </html>
        """

        when:
        // tag::no_alert[]
        withNoAlert { $("input", name: "dontShowAlert").click() }
        // end::no_alert[]

        then:
        true
    }
}
