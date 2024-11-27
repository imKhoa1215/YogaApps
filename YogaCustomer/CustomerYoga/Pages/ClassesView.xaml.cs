using Firebase.Database;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace CustomerYoga.Pages;

public partial class ClassesView : ContentPage
{
    private readonly FirebaseClient _firebaseClient; // Firebase client for interacting with the Firebase Realtime Database.
    private ObservableCollection<YogaClass> _allClasses; // Collection to hold all yoga classes.

    public string SearchText { get; set; } // Property bound to the search bar for filtering classes.
    public ICommand SearchCommand { get; } // Command for handling search functionality

    public ICommand OrderCommand { get; } // Command for handling the order action on a yoga class.

    // Constructor initializes Firebase connection, loads classes, and sets up commands.
    public ClassesView()
    {
        _firebaseClient = new FirebaseClient("https://yoga-485ab-default-rtdb.asia-southeast1.firebasedatabase.app"); 
        _allClasses = new ObservableCollection<YogaClass>();

        LoadClasses(); // Load classes from Firebase.

        // Initialize commands. These commands are bound to UI elements like buttons.
        OrderCommand = new Command<YogaClass>(PerformOrder);
        BindingContext = this; // Sets the BindingContext for data binding.
        InitializeComponent(); // Initializes the UI components defined in XAML.
    }

    // Loads yoga classes asynchronously from Firebase and updates the UI.
    private async void LoadClasses()
    {
        var classes = await GetClassesAsync(); // Fetch classes from Firebase.
        _allClasses = new ObservableCollection<YogaClass>(classes); // Update local collection.
        ClassesListView.ItemsSource = _allClasses; // Bind the collection to the ListView in the UI.
    }

    // Retrieves yoga classes from Firebase, processes them, and returns a list of YogaClass objects.
    private async Task<List<YogaClass>> GetClassesAsync()
    {
        var classes = await _firebaseClient
            .Child("Class") // Points to the "Class" node in Firebase.
            .OnceAsync<YogaClass>(); // Fetches all entries under "Class".

        // Transform the raw Firebase objects into YogaClass objects.
        return classes.Select(yogaClass => new YogaClass
        {
            Id = yogaClass.Key,
            Name = yogaClass.Object.Name,
            Date = yogaClass.Object.Date,
            CourseId = yogaClass.Object.CourseId,
            Comment = yogaClass.Object.Comment,
            Teacher = yogaClass.Object.Teacher
        }).ToList();
    }

    // Handles the text change event in the search bar to filter the displayed classes.
    private void OnSearchBarTextChanged(object sender, TextChangedEventArgs e)
    {
        var searchText = e.NewTextValue; // Get the new text from the search bar.

        if (string.IsNullOrEmpty(searchText))
        {
            // If search text is empty, display all classes.
            ClassesListView.ItemsSource = _allClasses;
        }
        else
        {
            // Filter classes based on the date containing the search text.
            var filteredClasses = _allClasses.Where(c => c.Date.Contains(searchText)).ToList();
            ClassesListView.ItemsSource = filteredClasses;
        }
    }

    // Handles the action when a class is ordered.
    private void PerformOrder(YogaClass yogaClass)
    {
        // Show a confirmation alert to the user.
        DisplayAlert("Order", $"You have ordered the class: {yogaClass.Name}", "OK");

        // Navigate to the shopping cart page with the selected yoga class.
        Navigation.PushModalAsync(new ShoppingCart(yogaClass), true);
    }
}