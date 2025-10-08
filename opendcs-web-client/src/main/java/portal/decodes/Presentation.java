/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package portal.decodes;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import portal.PortalBase;

/**
 * Represents the Presentation HttpServlet.
 *
 * @author Will Jonassen
 *
 */
@WebServlet("/presentation")
public class Presentation extends PortalBase {

    /**
     * Creates a new Presentation
     * @throws IOException 
     */
    public Presentation() throws IOException {
        super("/WEB-INF/app_pages/presentation.jsp", "decodes", "presentation");
    }
}