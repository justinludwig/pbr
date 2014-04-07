<!doctype html>
<html>
<head>
    <title>PBR Test</title>
    <style type="text/css">
        .test {
            color: red;
        }
    </style>
    <pbr:style>
        #inline-css {
            visibility: hidden;
            color: green;
        }
        #inline-css:before {
            visibility: visible;
            content: "Inline CSS: Passed!";
        }
    </pbr:style>
    <pbr:require modules="modernizr application" />
    <pbr:head />
</head>
<body>
    <h1>PBR Test</h1>
    <p id="test-js" class="test">Test JS: Failed :(</p>
    <p id="test-css" class="test">Test CSS: Failed :(</p>
    <p id="inline-js" class="test">Inline JS: Failed :(</p>
    <p id="inline-css" class="test">Inline CSS: Failed :(</p>
    <p>Image at <pbr:url module="image.application" />:
        <pbr:render module="image.application" />
        <pbr:img dir="images/test" file="app.jpg" width="16" height="16" alt="" />
    </p>
    <pbr:script>
        $(function() {
            $("#inline-js").css("color", "green").html("Inline JS: Passed!");
        });
    </pbr:script>
    <pbr:foot />
</body>
</html>
