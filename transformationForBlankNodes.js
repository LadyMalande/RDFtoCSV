function transform(message) {

    if (!message) return
    // Update header text
    const pattern3 = /_%3A/g;

    const updatedmessage2 = message.replace(pattern3, (match) => {
        return "_:"; // Remove the first two characters and the last character
    });
    const pattern = /<.*%23blankNodeValue%23_:/g;

    const message2 = updatedmessage2.replace(pattern, (match) => {
        return match.slice(-2); // Remove the first two characters and the last character
    });

    const pattern2 = /_:.*?>/g;

    const updatedmessage = message2.replace(pattern2, (match) => {
        return match.slice(0, -1); // Remove the first two characters and the last character
    });

    return updatedmessage
}

process.argv.forEach((val, index) => {
    if (index == 2) {

        const fs = require('fs');

        fs.readFile(val, (err, data) => {
            if (err) throw err;

            const doneData = transform(data.toString());

            // Log to console
            console.log(doneData)
        });

    }
});
