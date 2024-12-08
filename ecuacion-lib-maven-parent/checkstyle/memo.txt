====
    Copyright © 2012 ecuacion.jp (info@ecuacion.jp)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

- google_checks_ecuacion_changed.xml: Downloaded from the following URL. Filename changed.
　https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml

- changed point on google_checks_ecuacion_changed.xml:
　- Inside of <module name="SuppressionFilter">:  to refer "ecuacion_suppression_filter_for_google_checks_ecuacion_changed.xml".
    (Actually we wanted to avoid to change the config xml by setting ${org.checkstyle.google.suppressionfilter.config} from eclipse, 
     but we didn't know how to do it.
     And eclipse cs plugin doesn't seem to have suppressionFilter file settings.
     We also tried to set another config xml which has "SuppressionFilter" settings, it didn't work.)

- When you use checkstyle on eclipse, right-click the project -> Maven -> Update Project will set the checkstyle settings on eclipse(.checkstyle).

- ALERT: UPDATE ※eclipse-cs plugin to the latest version. Normal function checked with version 10.21.1.
