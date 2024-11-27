using Firebase.Database;
using System.Collections.ObjectModel;

namespace CustomerYoga.Pages;

public partial class ShoppingCart : ContentPage
{
    private readonly FirebaseClient _firebaseClient;
    private YogaClass yogaClass;

    public ShoppingCart()
    {
        InitializeComponent();
    }

    public ShoppingCart(YogaClass yogaClass)
    {
        _firebaseClient = new FirebaseClient("https://yoga-485ab-default-rtdb.asia-southeast1.firebasedatabase.app");

        InitializeComponent();

        this.yogaClass = yogaClass;

        BindingContext = yogaClass;
    }


    // Handles the checkout button click event.
    private async void OnCheckoutClicked(object sender, EventArgs e)
    {
        string email = EmailEntry.Text;     // Retrieve the email entered by the user.

        // Validate the email before proceeding.
        if (string.IsNullOrEmpty(email) || !IsValidEmail(email))
        {
            // Show an error message if the email is invalid or empty.
            await DisplayAlert("Invalid Email", "Please enter a valid email address.", "OK");
            return;
        }

        // Display a confirmation message for the checkout.
        await DisplayAlert("Checkout", "Thank you for your purchase " + yogaClass.Name, "OK");

        // Create a new order object with the user's email and selected yoga class.
        var order = new Order
        {
            Email = email,
            ClassOdered = yogaClass,
        };

        // Save the order to Firebase under the "orders" node.
        await _firebaseClient
            .Child("orders")
            .PostAsync(order);
    }

    // Validates an email address format.
    private bool IsValidEmail(string email)
    {
        try
        {
            // Use the System.Net.Mail.MailAddress class to validate the email format.
            var addr = new System.Net.Mail.MailAddress(email);
            return addr.Address == email;
        }
        catch
        {
            // Return false if the email is invalid.
            return false;
        }
    }
}