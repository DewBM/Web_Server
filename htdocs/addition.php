<!DOCTYPE html>
<html>
<head>
    <title>Sum Calculator</title>
    <link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
<div class="container">
    <h1>Sum Calculator</h1>

    <?php
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Retrieve input values from the form
        $num1 = $_POST["num1"];
        $num2 = $_POST["num2"];

        // Perform the calculation
        $sum = $num1 + $num2;

    }
    else if ($_SERVER["REQUEST_METHOD"] == "GET") {
        $num1 = $_GET["num1"];
        $num2 = $_GET["num2"];

        $sum = $num1 - $num2;
    }

    // Display the result
    echo "<p>The sum is: <span id='sum'>$sum</span></p>";
    ?>

    <a href="index.html">Go Back</a>
</div>
</body>
</html>

