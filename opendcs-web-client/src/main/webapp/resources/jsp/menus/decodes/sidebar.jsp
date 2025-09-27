<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%--
  ~  Copyright 2023 OpenDCS Consortium
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~       http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%-- Main sidebar --%>

<!--<script src="/webjars/bootstrap-sidebar/js/sidebar.js"></script>-->


<nav class="nav d-flex flex-column flex-shrink-0 p3" style="width: 30em;">
	<ul class=""list-unstyled ps-0">
		<li class="mb-1">
			<button class="btn btn-toggle d-inline-flex align-items-center rounded border-0 collapsed" data-bs-toggle="collapse" data-bs-target="#decodes-collapse" aria-expanded="true">
				Decodes
			</button>
			<div id="decodes-collapse" class="collapse show">
				<a class="nav-link active" aria-current="page" href="#">Platforms</a>
				<a class="nav-link" href="#">Sites</a>
				<a class="nav-link" href="#">Configs</a>
				<a class="nav-link" href="#">Presentation</a>
				<a class="nav-link" href="#">Routing</a>
				<a class="nav-link" href="#">Data Sources</a>
				<a class="nav-link" href="#">Netlists</a>
				<a class="nav-link" href="#">Schedule Entry</a>
			</div>
		</li>
		<li class="mb-1">
			<button class="btn btn-toggle d-inline-flex align-items-center rounded border-0 collapsed" data-bs-toggle="collapse" data-bs-target="#computations-collapse" aria-expanded="true">
				Computations
			</button>
			<div id="computations-collapse" class="collapse">
				<a class="nav-link" aria-current="page" href="#">Algorithms</a>
				<a class="nav-link" href="#">Computations</a>
				<a class="nav-link" href="#">Processes</a>
			</div>
		</li>
		<li class="mb-1">
			<button class="btn btn-toggle d-inline-flex align-items-center rounded border-0 collapsed" data-bs-toggle="collapse" data-bs-target="#reflist-collapse" aria-expanded="true">
				Reference List
			</button>
			<div id="reflist-collapse" class="collapse">
				<a class="nav-link" aria-current="page" href="#">Enumerations</a>
				<a class="nav-link" href="#">Engineering Units</a>
				<a class="nav-link" href="#">Unit Conversions</a>
				<a class="nav-link" href="#">Seasons</a>
			</div>
		</li>
	</ul>
</nav>
<%-- /main sidebar --%>
