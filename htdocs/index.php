<!DOCTYPE html>
<html>
<head>
    <title>Sum Calculator</title>
    <link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
<div class="container">
    <h1>Sum Calculator</h1>
    <form id="calculator-form" method="post" action="addition.php">
        <label for="num1">Number 1:</label>
        <input type="number" id="num1" name="num1" required><br>

        <label for="num2">Number 2:</label>
        <input type="number" id="num2" name="num2" required><br>

        <input type="submit" value="Calculate" id="calculate-button">
    </form>

    <div id="result">
        <p>The sum is: <span id="sum">0</span></p>
    </div>
</div>
</body>
</html>
