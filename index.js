const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.notifyCaller = functions.https.onCall((data, context) => {
    // ...
    const user_id = data.notified_caller_id;
    const call_id = data.call_id;
    const hero_name = data.hero_name;
    const message = data.message;

    console.log('Calling User ID: ' + user_id);
    console.log('Call ID: ' + call_id);
    console.log('Message: ' + message);
    console.log('Hero Name: ' + hero_name);

    const payload = {
        notification: {
            title: "A Hero Answered Your Call!",
            body: hero_name + " has responded to your call with ID: " + call_id + " with the following message: " + message
        }
    };

    return admin.messaging().sendToDevice(user_id, payload)
                .then(function (response) {
                console.log("Successfully sent message:", response);
                return true;
                })
                .catch(function (error) {
                    console.log("Error sending message:", error);
                    return true;
                });

  });