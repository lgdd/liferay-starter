import './main.css';
import {Elm} from './Main.elm';
import * as serviceWorker from './serviceWorker';

const darkTheme = "dark";
const apiHost = process.env.NODE_ENV === 'development' ? process.env.ELM_APP_API_HOST : "";

const elmApp = Elm.Main.init({
    node: document.getElementById('root'),
    flags: {
        "apiHost": apiHost
    }
});

elmApp.ports.initTheme.subscribe(() => {
    if (localStorage.getItem(darkTheme) === "true") {
        document.getElementsByClassName("toggle-switch-check")[0].click();
    }
});

elmApp.ports.toggleDark.subscribe(() => {
    document.body.classList.toggle(darkTheme);
    localStorage.setItem("dark", document.body.classList.contains(darkTheme));
});

elmApp.ports.copyToClipboard.subscribe((containerId) => {
    const node = document.getElementById(containerId);

    if (document.body.createTextRange) {
        const range = document.body.createTextRange();
        range.moveToElementText(node);
        range.select();
    } else if (window.getSelection) {
        const selection = window.getSelection();
        const range = document.createRange();
        range.selectNodeContents(node);
        selection.removeAllRanges();
        selection.addRange(range);
    } else {
        console.warn("Could not select text in node: Unsupported browser.");
    }
    document.execCommand("copy");
});

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
