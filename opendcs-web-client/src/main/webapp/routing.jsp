<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
	<jsp:include page="/resources/jsp/headers/decodes.jsp" />

	<body class="navbar-top">
		<!-- Main navbar -->
		<jsp:include page="/resources/jsp/menus/decodes/main.jsp" />   

	    <!-- Page content -->
	    <div class="page-content">
	
	        <jsp:include page="/resources/jsp/menus/decodes/sidebar.jsp" /> 
				
			<!-- Main content -->
			<div class="content-wrapper">
				<!-- Page header -->
				<div class="page-header page-header-light">
					<div class="page-header-content header-elements-md-inline">
						<div class="page-title d-flex">
							<h4><span class="font-weight-semibold">OpenDCS</span> - Routing</h4>
							<a href="#" class="header-elements-toggle text-default d-md-none"><i class="bi bi-three-dots-vertical"></i></a>
						</div>
					</div>  
				</div> <!-- /page header -->
			
				<!-- Content area -->
				<div class="content">
					<!-- Basic responsive configuration -->
					<div class="card large-padding">
						<div class="card-header header-elements-inline">
						</div>
						<table id="routingTable" class="table table-hover datatable-responsive tablerow-cursor w-100">
							<thead>
								<tr>
									<th>Id</th>
									<th>Name</th>
									<th>Data Source</th>
									<th>Consumer</th>
									<th>Last Modified</th>
									<th>Actions</th>
								</tr>
							</thead>
							<tbody id="tableBody">
								<!--This is where the data rows go-->
							</tbody>
						</table>
					</div> <!-- /basic responsive configuration -->
				</div> <!-- /Content area -->


				<jsp:include page="/resources/jsp/footers/decodes.jsp" /> 
				
				
			</div> <!-- /main content -->
		</div> <!-- /page content -->
	</body>


	<!-- Routing modal -->
	<div id="modal_routing" class="modal fade" tabindex="-1" data-bs-keyboard="false" data-bs-backdrop="static">
		<div class="modal-dialog modal-dialog-scrollable opendcs-modal-90">
			<div class="modal-content h-100">
				<div class="modal-header bg-secondary">
					<h6 class="modal-title" id="RoutingTitle">Routing</h6>
				</div>
				<div id="displayedRoutingSpecId" class="displayNone"></div>
				<div class="modal-body grey-background total_height-header-footer">
					<div class="row h-50">
						<div class="h-100 col-lg-4 p-1">
							<div class = "card col-12 h-100 pl-3 pr-3 pt-1 pb-1 opendcs_y_scrollable">
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Name:</label>
									<div class="col-lg-9">
										<input id="routingName" type="text" name="numbers" class="form-control" placeholder="Enter Routing Spec Name"></input>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Data Source:</label>
									<div class="col-lg-9">
										<select id="dataSourceSelect" class="selectpicker form-control">
										</select>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Destination:</label>
									<div class="col-lg-9">
										<select id="destinationSelect" class="selectpicker form-control">
											<option value="pipe" selected>pipe</option>
											<option value="file">file</option>
											<option value="directory">directory</option>
											<option value="cwms">cwms</option>
											<option value="socketclient">socketclient</option>
											<option value="opentsdb">opentsdb</option>
										</select>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Host/Port:</label>
									<div class="col-lg-9">
										<input id="destinationArg" type="text" class="form-control" placeholder="Host:Port Number">
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Output Format:</label>
									<div class="col-lg-9">
										<select id="outputFormatSelect" class="selectpicker form-control">
										</select>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Time Zone:</label>
									<div class="col-lg-9">
										<select id="timeZoneSelect" class="selectpicker form-control">
											<option selected></option>
										</select>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-lg-3">Presentation Group:</label>
									<div class="col-lg-9">
										<select id="presentationGroupSelect" class="selectpicker form-control">
											<option value=""></option>
										</select>
									</div>
								</div>
								<div class="form-group row">
									<div class="text-right nobr col-6">
									
										<div class="form-check form-switch">
											<label class="form-check-label float-left">
											In-line computations
											</label>
												<input type="checkbox" id="inlineComputationsCheckbox" class="form-check-input" >
												
											
										</div>
									</div>
									<div class="text-right nobr col-4">
										<div class="form-check form-switch">
											<label class="form-check-label float-left">
												Is Production
											</label>
												<input type="checkbox" id="isProductionCheckbox" class="form-check-input" >
												
											
										</div>
									</div>
								</div>
								
							</div>
						</div>
						<div class="h-100 col-lg-8 p-1">
							<div class="card col-12 h-100 pl-3 pr-3 pt-1 pb-1 opendcs_y_scrollable">
							<table>
								<caption class="captionTitleCenter">Date/Time</caption>
							</table>
							<div class="form-group smallMarginBottom row">
								<label class="col-form-label text-right col-2">Since:</label>
								<div class="nobr col-3">
									<select id="sinceSelect" class="selectpicker form-control nobr">
										<option  value="nowminus" selected>Now -</option>
										<option value="calendar">Calendar</option>
										<option value="filetime">File Time</option>
									</select>
								</div>
								<div id="sinceNowMinusSelectDiv" class="nobr col-3">
									<select id="sinceNowMinusSelect" class="selectpicker form-control nobr">
										<option>1 day</option>
										<option selected>3 hours</option>
										<option>2 hours</option>
										<option>1 hour</option>
										<option>30 minutes</option>
									</select>
								</div>
								<div id="sinceCalendarDiv" class="col-3 displayNone">
									<input id="sinceCalendar" class="form-control" type="datetime-local" name="datetime-local">
								</div>
								<div id="sinceFileDiv" class="col-5 displayNone">
										<input id="sinceFile" type="file" class="form-control-uniform displayNone">
										<div class="input-group">
											<input id="sinceFileTextbox" type="text" class="form-control" placeholder="File Path">
											<!-- 
											<span class="input-group-append">
												<button class="btn btn-light" type="button" id="chooseFileButton">Choose File</button>
											</span>
											-->
										</div>
								</div>
							</div>
							
							<div class="form-group smallMarginBottom row">
								<label class="col-form-label text-right col-2">Until:</label>
								<div class="nobr col-3">
									<select id="untilSelect" class="selectpicker form-control nobr">
										<option value="now" selected>Now</option>
										<option value="nowminus">Now -</option>
										<option value="calendar">Calendar</option>
										<option value="realtime">Real Time</option>
									</select>
								</div>
								
								<div class="text-left nobr col-5" id="untilNowTextDiv">
									<label>Stop after current data is retrieved.</label><br>
								</div>
								<div id="untilNowMinusSelectDiv" class="nobr col-3 displayNone">
									<select id="untilNowMinusSelect" class="selectpicker form-control nobr">
										<option>1 day</option>
										<option selected>3 hours</option>
										<option>2 hours</option>
										<option>1 hour</option>
										<option>30 minutes</option>
									</select>
								</div>
								<div id="untilCalendarDiv" class="col-3 displayNone">
									<input id="untilCalendar" class="form-control" type="datetime-local" name="datetime-local">
								</div>
								<div id="untilRealTimeDiv" class="text-left nobr col-5 displayNone">
									<input type="checkbox" id="realTimeCheckbox">
									<label for="realTimeCheckbox"> 30 sec delay to avoid duplicates</label><br>
								</div>
							</div>
							
							<div class="form-group smallMarginBottom row">
								<label class="col-form-label text-right col-2">Apply To:</label>
								<div class="nobr col-3">
									<select id="applyToSelect" class="selectpicker form-control nobr">
										<option value="Local Receive Time" selected>Local Receive Time</option>
										<option value="Platform Xmit Time" >Platform Xmit Time</option>
										<option value="Both">Both</option>
									</select>
								</div>
								<div class="text-left nobr col-5">
									<input type="checkbox" id="ascendingTimeOrderCheckbox" name="ascendingTimeOrderCheckbox">
									<label for="ascendingTimeOrderCheckbox"> Ascending time order (may slow retrievals)</label><br>
								</div>
							</div>
						</div>
						</div>
					</div>
					<div class="row h-50">
						<div class="col-lg-4 p-1 h-100">
							<div class="card col-lg-12 pl-3 pr-3 pt-1 pb-1 h-100">
								<table id="propertiesTable" resize_on_window_resize="96.8" class="table table-hover datatable-responsive">
									<caption class="captionTitleCenter">Properties
										<button class="btn btn-secondary float-right captionButton mt-1" id="addPropertyButton">+</button>
									</caption>
									<thead>
										<tr>
											<th>Name</th>
											<th>Value</th>
											<th>Actions</th>
										</tr>
									</thead>
									<tbody id="routingPropertiesTableBody"></tbody>
								</table>
							</div>
						</div>
						<div class="col-lg-5 p-1 h-100">
							
								<div class="card col-lg-12 pl-3 pr-3 pt-1 pb-1 h-100">
									
									<table id="platformSelectionTable" resize_on_window_resize="96.8" class="table table-hover datatable-responsive">
										<caption class="captionTitleCenter">Platform Selection
											<button class="btn btn-secondary float-right captionButton mt-1" id="addPlatformSelectionButton">+</button>
										</caption>
										<thead>
											<tr>
												<th>Type</th>
												<th>Value</th>
											</tr>
										</thead>
									</table>
								</div>
						</div>
						
						<div class="col-lg-3 p-1 h-100">
								<div class="card col-lg-12 pl-3 pr-3 pt-1 pb-1 h-100 opendcs_y_scrollable">
									<div class="form-group pt-2">
											<label class="font-weight-semibold">Platform/Message Types</label>
												
											<div class="form-check form-switch">
												<label class="form-check-label">
													<input type="checkbox" id="goesSelfTimedCheckbox" class="form-check-input" >
													GOES Self Timed
												</label>
											</div>
											<div class="form-check form-switch">
												<label class="form-check-label">
													<input type="checkbox" id="goesRandomCheckbox" class="form-check-input" >
													GOES Random
												</label>
											</div>
											<div class="form-check form-switch">
												<label class="form-check-label">
													<input type="checkbox" id="qualityNotificationsCheckbox" class="form-check-input" >
													Quality Notifications
												</label>
											</div>
											<div class="row">
												<div class="col-6">
													<div class="form-check form-switch">
														<label class="form-check-label">
															<input type="checkbox" id="goesSpacecraftCheckbox" class="form-check-input" >
															GOES Spacecraft
														</label>
													</div>
												</div>
												<div class="col-6">
													<select id="goesSpacecraftSelector" class="selectpicker form-control nobr">
														<option value="east">East</option>
														<option value="west">West</option>
													</select>
												</div>
											</div>
											<div class="form-check form-switch">
												<label class="form-check-label">
													<input type="checkbox" id="iridiumCheckbox" class="form-check-input" >
													Iridium
												</label>
											</div>
											<div class="form-check form-switch">
												<label class="form-check-label">
													<input type="checkbox" id="networkModemDcpCheckbox" class="form-check-input" >
													Network/Modem DCP
												</label>
											</div>
											<div class="row">
												<div class="col-6">
													<div class="form-check form-switch">
														<label class="form-check-label">
															<input type="checkbox" id="parityCheckbox" class="form-check-input" >
															Parity
														</label>
													</div>
												</div>
													<div class="col-6">
														<select id="paritySelector" class="selectpicker form-control nobr">
															<option value="good">Good</option>
															<option value="bad">Bad</option>
														</select>
													</div>
											</div>
										</div>
									</div>
								
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" id="cancelRoutingModalButton" data-bs-dismiss="modal">Cancel</button>
					<button type="button" class="btn btn-success" id="saveRoutingModalButton">Save</button>
				</div>
			</div>
		</div>
	</div>
	<!-- /routing modal -->

	<!-- Platform selection modal -->
	<div id="modal_platformselection" class="modal fade" tabindex="-1" data-bs-keyboard="false" data-bs-backdrop="static">
	    <div class="modal-dialog modal-dialog-scrollable opendcs-modal-90">
	        <div class="modal-content">
	            <div class="modal-header bg-secondary">
	            	<h6 class="modal-title" id="modalTitle"><span id="modalMainTitle">Platform Selection</span><span id="modalSubTitle"></span></h6>
	            </div>
				<div id="displayedId" class="displayNone"></div>
	            <div class="modal-body grey-background total_height-header-footer">
	                <div class="row h-50">
	                	<div class="col-lg-12 p-1 h-100">
		                	<div class="card pl-3 pr-3 pt-1 pb-1 h-100">
				                <table id="platformSelectionTable2" class="table table-hover datatable-responsive" resize_on_window_resize="94">
				                    <thead>
				                        <tr>
				                        	<th>Platform ID</th>
				                            <th>Platform Name</th>
				                            <th>Agency</th>
				                            <th>Transport Medium - ID</th>
				                            <th>Config</th>
				                            <th>Description</th>
				                        </tr>
				                    </thead>
				                </table>
			                </div>
		                </div>
	                </div>
	                <div class="row h-50">
	                	<div class="col-lg-8 p-1 h-100">
		                <div class="card pl-3 pr-3 pt-1 pb-1 h-100">
			                <table id="netlistSelectionTable" class="table table-hover datatable-responsive h-100" resize_on_window_resize="95">
			                    <thead>
			                        <tr>
			                        	<th>ID</th>
			                        	<th>List Name</th>
			                            <th>Medium Type</th>
			                            <th># of Platforms</th>
			                        </tr>
			                    </thead>
			                </table>
		                </div>
		                </div>
		                
	                	<div class="col-lg-4 p-1 h-100">
	                	<div class="card pl-3 pr-3 pt-1 pb-1 h-100">
			                <table id="goesChannelTable" class="table table-hover datatable-responsive h-100" resize_on_window_resize="96">
			                <caption id="siteNamesCaption" class="captionTitleCenter">
									GOES Channels
									<button function="addBlankRow" class="btn btn-secondary float-right captionButton"
										id="addGoesChannelButton">+</button>
								</caption>
			                    <thead>
			                        <tr>
			                        	<th>Channel</th>
			                        	<th>Actions</th>
			                        </tr>
			                    </thead>
			                </table>
		                </div>
		                </div>
	                </div>
	            </div>
	      
	            <div class="modal-footer">
	                <button type="button" class="btn btn-secondary" id="cancelPlatformModalButton" data-bs-dismiss="modal">Cancel</button>
	                <button type="button" class="btn btn-success" id="addPlatformModalButton">Update</button>
	            </div>
	        </div>
	    </div>
	</div>
	<!-- /platform selection modal -->


<jsp:include page="/resources/jsp/includes/decodes.jsp" />


<script src="../resources/js/datatables/datatables.js"></script>
<script src="../resources/js/datatables/helpers.js"></script>
<script src="../resources/js/lib/time.js"></script>
<script src="../resources/js/lib/dom_utilities.js"></script>
<script src="../resources/js/lib/object_utilities.js"></script>
<script src="../resources/js/lib/date_utilities.js"></script>
<script src="../resources/js/routing.js"></script>

</html>