using Firebase.Database;
using System.Collections.ObjectModel;
using System.Windows.Input;

namespace CustomerYoga.Pages;

public partial class ViewOrder : ContentPage
{
	private readonly FirebaseClient _firebaseClient;
	private ObservableCollection<Order> _allOrdered;

	public string SearchText { get; set; }
	public ICommand SearchCommand { get; }

	public ViewOrder()
	{
		_firebaseClient = new FirebaseClient("https://yoga-485ab-default-rtdb.asia-southeast1.firebasedatabase.app");
		_allOrdered = new ObservableCollection<Order>();

		LoadOrders();

		InitializeComponent();
		BindingContext = this;
	}

	// Loads all orders asynchronously from Firebase and updates the ListView.
	private async void LoadOrders()
	{
		var allOrdered = await GetOrdersAsync(); // Fetch all orders from Firebase.
		_allOrdered = new ObservableCollection<Order>(allOrdered); // Update the local collection with fetched orders.
		OrderListView.ItemsSource = _allOrdered; // Bind the orders collection to the ListView in the UI.
	}

	// Retrieves all orders from Firebase, processes them, and returns a list of Order objects.
	private async Task<List<Order>> GetOrdersAsync()
	{
		var classes = await _firebaseClient
			.Child("orders") // Points to the "orders" node in Firebase.
			.OnceAsync<Order>(); // Fetches all entries under "orders".

		// Transform Firebase data into a list of Order objects.
		return classes.Select(order => new Order
		{
			ClassOdered = order.Object.ClassOdered, // The yoga class associated with the order.
			Email = order.Object.Email, // The email of the user who placed the order.
		}).ToList();
	}

	// Handles text changes in the search bar to filter displayed orders by email.
	private void OnSearchBarTextChanged(object sender, TextChangedEventArgs e)
	{
		var searchText = e.NewTextValue; // Get the new search text entered by the user.
		if (string.IsNullOrEmpty(searchText))
		{
			// If the search text is empty, display all orders.
			OrderListView.ItemsSource = _allOrdered;
		}
		else
		{
			// Filter orders based on an exact match of the email.
			var filteredClasses = _allOrdered.Where(c => c.Email.Equals(searchText)).ToList();
			OrderListView.ItemsSource = filteredClasses; // Update the ListView with the filtered results.
		}
	}


}