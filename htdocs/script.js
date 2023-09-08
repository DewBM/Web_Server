document.addEventListener("DOMContentLoaded", function () {
    const num1Input = document.getElementById("num1");
    const num2Input = document.getElementById("num2");
    const calculateButton = document.getElementById("calculate-button");
    const sumResult = document.getElementById("sum");

    calculateButton.addEventListener("click", function () {
        const num1 = parseFloat(num1Input.value);
        const num2 = parseFloat(num2Input.value);

        if (!isNaN(num1) && !isNaN(num2)) {
            const sum = num1 + num2;
            sumResult.textContent = sum;
        } else {
            sumResult.textContent = "Invalid input";
        }
    });
});
