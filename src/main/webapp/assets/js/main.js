document.addEventListener("DOMContentLoaded", function () {
    const encodeForm = document.getElementById("encodeForm");
    const decodeForm = document.getElementById("decodeForm");
    const resultDiv = document.getElementById("result");

    // Handle encode form submission
    encodeForm.addEventListener("submit", function (event) {
        event.preventDefault();
        encodeMessage();
    });

    // Handle decode form submission
    decodeForm.addEventListener("submit", function (event) {
        event.preventDefault();
        decodeMessage();
    });
});

/**
 * Encode a message into an image.
 */
function encodeMessage() {
    const imageInput = document.getElementById("image");
    const messageInput = document.getElementById("message");

    // Validate inputs
    if (!imageInput.files[0]) {
        alert("Please upload an image.");
        imageInput.focus();
        return;
    }
    if (!messageInput.value) {
        alert("Please enter a message.");
        messageInput.focus();
        return;
    }

    const formData = new FormData();
    formData.append("image", imageInput.files[0]);
    formData.append("message", messageInput.value);

    // Show loading message
    resultDiv.innerHTML = "<div class='loading'>Encoding... Please wait.</div>";

    // Send the request to the encode endpoint
    fetch("/projectAppSec-1.0-SNAPSHOT/api/steganography/encode", {
        method: "POST",
        body: formData,
    })
        .then((response) => {
            if (!response.ok) {
                return response.text().then((text) => {
                    throw new Error(text || "Failed to encode message.");
                });
            }
            return response.blob();
        })
        .then((blob) => {
            // Display the encoded image
            const url = URL.createObjectURL(blob);
            const img = document.createElement("img");
            img.src = url;
            img.alt = "Encoded Image";

            // Provide a download link
            const link = document.createElement("a");
            link.href = url;
            link.download = "encoded.png";
            link.textContent = "Download Encoded Image";
            link.className = "download-link";

            // Clear previous results and display new ones
            resultDiv.innerHTML = "";
            resultDiv.appendChild(img);
            resultDiv.appendChild(link);
        })
        .catch((error) => {
            console.error("Error:", error);
            resultDiv.innerHTML = `<div class='error'>Failed to encode message. ${error.message}</div>`;
        });
}

/**
 * Decode a message from an encoded image.
 */
function decodeMessage() {
    const encodedImageInput = document.getElementById("encodedImage");

    // Validate input
    if (!encodedImageInput.files[0]) {
        alert("Please upload an encoded image.");
        encodedImageInput.focus();
        return;
    }

    const formData = new FormData();
    formData.append("encodedImage", encodedImageInput.files[0]);

    // Show loading message
    resultDiv.innerHTML = "<div class='loading'>Decoding... Please wait.</div>";

    // Send the request to the decode endpoint
    fetch("/projectAppSec-1.0-SNAPSHOT/api/steganography/decode", {
        method: "POST",
        body: formData,
    })
        .then((response) => {
            if (!response.ok) {
                return response.text().then((text) => {
                    throw new Error(text || "Failed to decode message.");
                });
            }
            return response.text();
        })
        .then((message) => {
            // Display the decoded message
            resultDiv.innerHTML = `<div class='success'>Decoded Message: ${message}</div>`;
        })
        .catch((error) => {
            console.error("Error:", error);
            resultDiv.innerHTML = `<div class='error'>Failed to decode message. ${error.message}</div>`;
        });
}