<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
	<%@include file="/WEB-INF/common/header.jspf" %>

	<body class="navbar-top">
		<%@include file="/WEB-INF/common/top-bar.jspf" %>

	    <!-- Page content -->
	    <div class="page-content">

	        <%@include file="/WEB-INF/common/sidebar.jspf" %>

			<!-- Main content -->
			<div class="content-wrapper">
				<!-- Page header -->
				<div class="page-header page-header-light">
					<div class="page-header-content header-elements-md-inline">
						<div class="page-title d-flex">
							<h4><span class="font-weight-semibold">OpenDCS</span> - Enumerations</h4>
							<a href="#" class="header-elements-toggle text-default d-md-none"><i class="bi bi-three-dots-vertical"></i></a>
						</div>
					</div>
				</div> <!-- /page header -->

				<!-- Content area -->
				<div class="content">
					<!-- Basic responsive configuration -->
					<div class="card large-padding h-100">
						<div class="form-group row mb-2">
							<label class="col-form-label col-lg-5 text-right">Enumeration</label>
		                    <div class="col-lg-3">
		                      <select id="enumerationSelectbox" class="selectpicker form-control">
		                        </select>
		                    </div>
		                    <div class="col-lg-4">
		                    	<button type="button" class="btn btn-success float-right" id="saveButton">Save</button>
		                    </div>
						</div>
						<table id="enumerationTable" class="table table-hover datatable-responsive w-100">
							<caption class="captionTitleCenter">Enumerations
								<button class="btn btn-secondary float-right captionButton" id="addEnumerationButton">+</button>
							</caption>
							<thead>
								<tr>
									<th></th>
									<th>Default</th>
									<th>Name</th>
									<th>Description</th>
									<th>Java Class (optional)</th>
									<th>Options</th>
									<th>Actions</th>
								</tr>
							</thead>
						</table>
					</div> <!-- /basic responsive configuration -->
				</div> <!-- /Content area -->


				<%@include file="/WEB-INF/common/footer.jspf" %>

			</div> <!-- /main content -->
		</div> <!-- /page content -->
	</body>



	<%@include file="/WEB-INF/common/scripts.jspf" %>
<<<<<<< HEAD
=======
	
	<script src="/webjars/switchery/switchery.js"></script>
	<script src="/webjars/bootstrap-switch/js/bootstrap-switch.min.js"></script>
>>>>>>> 42b8bf4 (Cleanup fragement html for missing tags and end tags.)
	<script src="../resources/js/lib/date_utilities.js"></script>
	<script src="../resources/js/enumerations.js"></script>
</html>