let currenciesList = [];
let isLoggedIn = false;

$("#login-form").submit((e) => {
        e.preventDefault();

        if (!isLoggedIn) {
            fetch("http://localhost:8080/auth/sign-in", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    username: $("#login-username").val(),
                    password: $("#password").val(),
                })
            })
                .then(resp => {
                    if (!resp.ok) {
                        return resp.json().then(errData => {
                            throw new Error(errData.message || "Login failed");
                        });
                    }
                    return resp.json();
                })
                .then(data => {
                    localStorage.setItem("token", data.token);
                    isLoggedIn = true;
                    toggleLoginState();
                    if (data.role === "ROLE_ADMIN") {
                        $('#adminTools').removeClass("d-none")
                    }
                    connectWebsocket();
                    $('#authorized').removeClass("d-none")
                })
                .catch(err => {
                    alert("Login failed: " + err.message);
                });
        } else {
            localStorage.removeItem("token");
            isLoggedIn = false;
            toggleLoginState();
        }
    }
)

function toggleLoginState() {
    if (isLoggedIn) {
        $('#login-btn').text("Logout");
        $('#login-username').attr("disabled", true);
        $('#password').attr("disabled", true);
    } else {
        $('#login-btn').text("Login");
        $('#login-username').attr("disabled", false);
        $('#password').attr("disabled", false);
    }
}

async function connectWebsocket() {
    let stompClient = null;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    const token = localStorage.getItem('token');

    stompClient.connect({Authorization: `Bearer ${token}`}, (frame) => {
        console.log('Connected:', frame);

        stompClient.subscribe('/topic/balances', (message) => {
            updateBalances(JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/exchange-rates', (message) => {
            updateExchangeRates(JSON.parse(message.body));
        });

        stompClient.subscribe('/user/queue/messages', (message) => {
            console.log("!!usr: " + message.body)
            showMessages()
            show(message.body);
        });
    });
    await fetchBalances();
    await fetchExchangeRates();
}

function showMessages() {
    $("#websocket").removeClass("d-none")
}

async function fetchBalances() {
    try {
        const data = await sendRequest('/api/balances', 'GET');
        updateBalances(data);
    } catch (error) {
        console.error("Error fetching balances:", error);
    }
}

async function fetchCurrencies() {
    try {
        let newCurrenciesList = await sendRequest('/api/currencies', 'GET');

        const areListsEqual = (arr1, arr2) => {
            if (arr1.length !== arr2.length) return false;

            const sorted1 = [...arr1].sort((a, b) => JSON.stringify(a).localeCompare(JSON.stringify(b)));
            const sorted2 = [...arr2].sort((a, b) => JSON.stringify(a).localeCompare(JSON.stringify(b)));

            return sorted1.every((item, index) => JSON.stringify(item) === JSON.stringify(sorted2[index]));
        };

        if (!areListsEqual(newCurrenciesList, currenciesList)) {
            currenciesList = [...newCurrenciesList];

            await updateCurrencySelects();
        }
    } catch (error) {
        console.error("Error fetching currencies:", error);
    }
}

async function fetchExchangeRates() {
    await fetchCurrencies()
    if (currenciesList.length === 0) {
        console.error("Currencies list is empty.");
        return;
    }

    const exchangeRates = {};
    const filteredCurrencies = currenciesList.filter(currency => currency.code !== 'UAH');

    const fetchPromises = filteredCurrencies.map(async currency => {
        try {
            exchangeRates[`${currency.code}-UAH`] = await sendRequest(
                `/api/exchange-rates/rate?fromCurrency=${currency.code}&toCurrency=UAH`,
                'GET'
            );
        } catch (error) {
            console.error(`Error fetching rate for ${currency.code}-UAH:`, error);
        }
    });

    await Promise.all(fetchPromises);
    await updateExchangeRates(exchangeRates);
}

async function updateCurrencySelects() {
    const selectIds = ['fromCurrencyRate', 'fromCurrency', 'toCurrencyRate', 'toCurrency', 'currencyCodeAddAmount'];
    selectIds.forEach(selectId => {
        const selectElement = document.getElementById(selectId);
        if (!selectElement) return;

        selectElement.innerHTML = '<option value="" disabled selected>Select currency</option>';

        currenciesList.forEach(currency => {
            const option = document.createElement('option');
            option.value = currency.code;
            option.textContent = currency.code;
            selectElement.appendChild(option);
        });
    });
    const filteredSelectIds = ['fromCurrencyRateByDate', 'fromCurrencyRateSet', 'currencyCodeDel'];
    filteredSelectIds.forEach(selectId => {
        const selectElement = document.getElementById(selectId);
        if (!selectElement) return;

        selectElement.innerHTML = '<option value="" disabled selected>Select currency</option>';

        const filteredCurrencies = currenciesList.filter(currency => currency.code !== 'UAH');

        filteredCurrencies.forEach(currency => {
            const option = document.createElement('option');
            option.value = currency.code;
            option.textContent = currency.code;
            selectElement.appendChild(option);
        });
    });
}

function updateBalances(balances) {
    const balancesDiv = document.getElementById("balances");
    balancesDiv.innerHTML = '';

    for (const [currency, balance] of Object.entries(balances)) {
        const balanceItem = document.createElement("div");
        const rounded = Math.floor(balance * 100) / 100;
        balanceItem.classList.add("item");
        balanceItem.innerHTML = `<div>${currency}: ${rounded}</div>`;
        balancesDiv.appendChild(balanceItem);
    }
    fetchCurrencies()
}

async function updateExchangeRates(exchangeRates) {
    const ratesDiv = document.getElementById("exchange-rates");
    ratesDiv.innerHTML = '';

    for (const [pair, rate] of Object.entries(exchangeRates)) {
        if (rate === undefined) {
            console.warn(`Rate for ${pair} is undefined. Skipping.`);
            continue;
        }

        const rateItem = document.createElement("div");
        rateItem.classList.add("item");
        rateItem.innerHTML = `<div>${pair}: ${rate}</div>`;
        ratesDiv.appendChild(rateItem);
    }
    await fetchCurrencies();
}

function show(message) {
    if (message.includes("Rate changed")) {
        $("#confirm-reject").removeClass("d-none")

        const match = message.match(/\d+/);
        if (match) {
            $('#reqId').val(match[0]);
        }
    }
    const response = document.getElementById('message-output');
    const p = document.createElement('p');
    p.innerHTML = "" + message;
    response.appendChild(p);
    response.scrollTop = response.scrollHeight;
}

async function sendRequest(endpoint, method, body = null) {
    let headers;
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };

    const options = {method, headers};
    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(endpoint, options);
    console.log("!!: ", response)
    if (!response.ok) {
        if (response.status === 401) {
            isLoggedIn = false;
            toggleLoginState();
            alert("Session expired. Please log in again.");
            return;
        }
        try {
            const errorData = await response.json();
            alert(errorData.message || "An error occurred\nStatus: " + response.status + "\n" + errorData.amount);
        } catch (e) {
            const errorText = await response.text();
            alert(errorText);
        }
    } else {
        return response.json();
    }
}

async function getExchangeRate() {
    const fromCurrency = document.getElementById('fromCurrencyRate').value;
    const toCurrency = document.getElementById('toCurrencyRate').value;

    const response = await sendRequest(`/api/exchange-rates/rate?fromCurrency=${fromCurrency}&toCurrency=${toCurrency}`, 'GET');
    if (response) alert(`Rete ${fromCurrency} -> ${toCurrency}: ${response}`);
}

async function getExchangeRateByPeriod() {
    const fromCurrency = document.getElementById('fromCurrencyRateByDate').value;
    const period = document.querySelector('input[name="period"]:checked').value;

    const response = await sendRequest(`/api/exchange-rates/max-min?fromCurrency=${fromCurrency}&&period=${period}`, 'GET');
    if (response) alert(`Max Rate: ${response.maxRate}\nMin Rate: ${response.minRate}`);
}

async function createExchangeRequest() {
    const fromCurrency = document.getElementById('fromCurrency').value;
    const toCurrency = document.getElementById('toCurrency').value;
    const amount = document.getElementById('amount').value;

    const response = await sendRequest('/exchange/create', 'POST', {
        fromCurrency,
        toCurrency,
        amount: parseFloat(amount)
    });
    alert(`Exchange request created: ID ${response.id}, status: ${response.status}`);
}

async function registration() {
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    const email = document.getElementById('regEmail').value;

    const response = await sendRequest('/auth/sign-up', 'POST', {username, email, password});
    if (response) alert("Registration successfully completed!");
}

async function blockUser() {
    const username = document.getElementById('usernameToBlock').value;
    const response = await sendRequest(`/admin/block?username=${username}`, 'PUT');
    if (response) alert(response.message);
}

async function unblockUser() {
    const username = document.getElementById('usernameToUnblock').value;
    const response = await sendRequest(`/admin/unblock?username=${username}`, 'PUT');
    if (response) alert(response.message);
}

async function setRoleAdmin() {
    const username = document.getElementById('setRoleAdmin').value;
    const response = await sendRequest(`/admin/set-admin?username=${username}`, 'PUT');
    if (response) alert(response.message);
}

async function setRoleUser() {
    const username = document.getElementById('setRoleUser').value;
    const response = await sendRequest(`/admin/set-user?username=${username}`, 'PUT');
    if (response) alert(response.message);
}

async function confirm() {
    const reqId = document.getElementById('reqId').value;
    await sendRequest(`/exchange/approve?reqId=${reqId}&isApproved=true`, 'POST');
}

async function reject(exchange_rates) {
    const reqId = document.getElementById('reqId').value;
    await sendRequest(`/exchange/approve?reqId=${reqId}&isApproved=false`, 'POST');
}

async function addCurrency() {
    const currencyCode = document.getElementById('currencyCodeAdd').value;
    await sendRequest(`/api/currencies?currencyCode=${currencyCode}`, 'POST');
}

async function deleteCurrency() {
    const currencyCode = document.getElementById('currencyCodeDel').value;
    await sendRequest(`/api/currencies?currencyCode=${currencyCode}`, 'DELETE');
}

async function setExchangeRate() {
    const fromCurrency = document.getElementById('fromCurrencyRateSet').value;
    const rate = document.getElementById('setRate').value;
    const amount = parseFloat(rate);
    await sendRequest(`/api/exchange-rates/set-rate?fromCurrency=${fromCurrency}&rate=${amount}`, 'POST');
}

async function addAmount() {
    const currencyCode = document.getElementById('currencyCodeAddAmount').value;
    const amount = parseFloat(document.getElementById('addAmount').value);
    await sendRequest(`/api/balances?currencyCode=${currencyCode}&amount=${amount}`, 'POST');
}