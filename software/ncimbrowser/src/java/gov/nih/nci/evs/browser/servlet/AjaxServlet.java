package gov.nih.nci.evs.browser.servlet;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by NGIT and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIT
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history
 *     Initial implementation kim.ong@ngc.com
 *
 */

import org.json.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import gov.nih.nci.evs.browser.utils.*;
import org.apache.log4j.*;
import org.LexGrid.concepts.Entity;

import gov.nih.nci.evs.browser.common.*;

import javax.faces.context.*;

public final class AjaxServlet extends HttpServlet {
    private static Logger _logger = Logger.getLogger(AjaxServlet.class);

    /**
     * Validates the Init and Context parameters, configures authentication URL
     *
     * @throws ServletException if the init parameters are invalid or any other
     *         problems occur during initialisation
     */
    public void init() throws ServletException {

    }

    /**
     * Route the user to the execute method
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        execute(request, response);
    }

    /**
     * Route the user to the execute method
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        execute(request, response);
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */

    public void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

			 //[NCIM-209] AppScan Performance.
			 boolean retval = HTTPUtils.validateRequestParameters(request);
			 if (!retval) {
				 try {
					 String nextJSP = "/pages/appscan_response.jsf";
					 RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
					 dispatcher.forward(request,response);
					 return;

				 } catch (Exception ex) {
					 ex.printStackTrace();
				 }
			 }

        // Determine request by attributes
        String action = HTTPUtils.cleanXSS((String)request.getParameter("action"));// DataConstants.ACTION);
        if (action == null) action = "concept";
        String node_id = HTTPUtils.cleanXSS((String)request.getParameter("ontology_node_id"));// DataConstants.ONTOLOGY_NODE_ID);
        String ontology_display_name =
            HTTPUtils.cleanXSS((String)request.getParameter("ontology_display_name"));// DataConstants.ONTOLOGY_DISPLAY_NAME);
        String ontology_source = HTTPUtils.cleanXSS((String)request.getParameter("ontology_source"));

        long ms = System.currentTimeMillis();
        if (action.equals("expand_tree")) {
            String node_id_0 = node_id;
            if (node_id != null && ontology_display_name != null) {
                int pos = node_id.indexOf("|");
                if (pos != -1) {
                    //String parent_id =
                    //    node_id.substring(pos + 1, node_id.length());
                    //node_id = node_id.substring(0, pos);
                    response.setContentType("text/html");
                    response.setHeader("Cache-Control", "no-cache");
                    JSONObject json = new JSONObject();
                    JSONArray nodesArray = null;
                    try {
                        // KLO, 041210
                        // nodesArray =
                        // CacheController.getInstance().getRemainingSubconcepts(ontology_display_name,
                        // null, parent_id, node_id);
                        nodesArray = CacheController.getInstance().getRemainingSubconcepts(node_id_0);

                        if (nodesArray != null) {
                            json.put("nodes", nodesArray);
                        }
                    } catch (Exception e) {
						e.printStackTrace();
                    }

                    response.getWriter().write(json.toString());
                    _logger.debug("Run time (milliseconds): "
                        + (System.currentTimeMillis() - ms));
                    return;
                }
                response.setContentType("text/html");
                response.setHeader("Cache-Control", "no-cache");
                JSONObject json = new JSONObject();
                JSONArray nodesArray = null;
                try {
                    if (ontology_source == null) {
                        nodesArray =
                            CacheController.getInstance().getSubconcepts(
                                ontology_display_name, null, node_id);
                    } else {
						/*
                        nodesArray =
                            CacheController.getInstance()
                                .getSubconceptsBySource(ontology_display_name,
                                    null, node_id, ontology_source);
                        */
                        nodesArray = CacheController.getInstance().getRemainingSubconcepts(node_id_0 + "|" + ontology_source + "|0");
                    }

                    if (nodesArray != null) {
                        json.put("nodes", nodesArray);
                    }

                } catch (Exception e) {
                }
                response.getWriter().write(json.toString());
                _logger.debug("Run time (milliseconds): "
                    + (System.currentTimeMillis() - ms));
                return;
            }
        }

        else if (action.equals("search_tree")) {
            if (node_id != null && ontology_display_name != null) {
                response.setContentType("text/html");
                response.setHeader("Cache-Control", "no-cache");
                /*
                 * JSONObject json = new JSONObject();
                 *
                 * try { String max_tree_level_str = null; int maxLevel = -1;
                 * try { max_tree_level_str =
                 * NCImBrowserProperties.getInstance()
                 * .getProperty(NCImBrowserProperties.MAXIMUM_TREE_LEVEL);
                 * maxLevel = Integer.parseInt(max_tree_level_str); } catch
                 * (Exception ex) { }
                 *
                 * JSONArray rootsArray = null; rootsArray =
                 * CacheController.getInstance
                 * ().getPathsToRoots(ontology_display_name, null, node_id,
                 * ontology_source, true, maxLevel); if (rootsArray.length() ==
                 * 0) {//_logger.debug(
                 * "AjaxServlet getPathsToRoots finds no path -- calling getRootConceptsBySource..."
                 * ); //rootsArray =
                 * CacheController.getInstance().getRootConceptsBySource
                 * (ontology_display_name, null, ontology_source); }
                 *
                 * json.put("root_nodes", rootsArray); } catch (Exception e) {
                 * e.printStackTrace(); }
                 *
                 * response.getWriter().write(json.toString());
                 * _logger.debug("search_tree: " + json.toString());
                 */
                String t =
                    CacheController.getInstance().getPathsToRootsExt(
                        ontology_display_name, null, node_id, ontology_source,
                        false);
                response.getWriter().write(t);
                _logger.debug("Run time (milliseconds): "
                    + (System.currentTimeMillis() - ms));
                return;
            }
        }

        else if (action.equals("build_tree")) {
            if (ontology_display_name == null)
                ontology_display_name = "NCI Thesaurus";

            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");

            JSONObject json = new JSONObject();
            JSONArray nodesArray = null;// new JSONArray();

System.out.println("Build tree -- step 1");

            try {
                if (ontology_source == null
                    || ontology_source.compareTo("null") == 0) {

						System.out.println("Build tree -- step 2 calling CacheController.getInstance().getRootConcepts ");


                    nodesArray =
                        CacheController.getInstance().getRootConcepts(
                            ontology_display_name, null);
                } else {

					System.out.println("Build tree -- step 3 calling CacheController.getInstance().getSourceRoots");


                    nodesArray =
                        CacheController.getInstance().getSourceRoots(
                            ontology_display_name, null, ontology_source, true);
                    // nodesArray =
                    // CacheController.getInstance().getRootConceptsBySource(ontology_display_name,
                    // null, ontology_source);
                }

                System.out.println("Build tree -- step 4");


                if (nodesArray != null) {

					System.out.println("nodesArray != null");


                    json.put("root_nodes", nodesArray);
                } else {
					System.out.println("nodesArray == null ??? ");
				}
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.getWriter().write(json.toString());
            _logger.debug("Run time (milliseconds): "
                + (System.currentTimeMillis() - ms));
            return;
        }

        else if (action.equals("concept")) {
 			String concept_detail_scheme = HTTPUtils.cleanXSS((String)request.getParameter("dictionary"));
			String concept_detail_code = HTTPUtils.cleanXSS((String)request.getParameter("code"));
			String concept_detail_type = HTTPUtils.cleanXSS((String)request.getParameter("type"));

			Entity c = DataUtils.getConceptByCode(
							Constants.CODING_SCHEME_NAME, null, null, concept_detail_code);

			request.getSession().setAttribute("code", concept_detail_code);
			request.getSession().setAttribute("concept", c);
			request.getSession().setAttribute("type", "properties");
			request.getSession().setAttribute("new_search", Boolean.TRUE);

			response.setContentType("text/html;charset=utf-8");

			String response_page_url = request.getContextPath()
			                         + "/pages/concept_details.jsf?dictionary="
									 + concept_detail_scheme
									 + "&code="
									 + concept_detail_code
									 + "&type="
									 + concept_detail_type;
	        response.sendRedirect(response_page_url);
	    }


    }
}
