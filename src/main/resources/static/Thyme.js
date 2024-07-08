import globalStyles from './styles/global.css';
import * as Util from './modules/Util.js';
import { Locale } from './modules/Locale.js';
import { Http } from './modules/Http.js';
import { Form } from './modules/Form.js';
import { Channel } from './modules/Channel.js';
import { Button } from './components/Button.js';
import { Calendar } from './components/Calendar.js';
import { Checkbox } from './components/Checkbox.js';
import { CheckGroup } from './components/CheckGroup.js';
import { Dialog } from './components/Dialog.js';
import { Field } from './components/Field.js';
import { Formula } from './components/Formula.js';
import { Select } from './components/Select.js';
import { Switch } from './components/Switch.js';
import { TextBox } from './components/TextBox.js';
import { Toast } from './components/Toast.js';
import { Toptray } from './components/Toptray.js';
import { Upload } from './components/Upload.js';
import { Waterfall } from './components/Waterfall.js';
import { Progress } from './components/Progress.js';

class Thyme {

    static util = Util;
    static http = Http;
    static form = Form;
    static Progress = Progress;
    static Channel = Channel;
    static #toast;

    static {
        Util.enhanceElements();
        document.head.append(Util.createStyle(globalStyles));

        Util.registerComponent('th-button', Button);
        Util.registerComponent('th-calendar', Calendar);
        Util.registerComponent('th-checkbox', Checkbox);
        Util.registerComponent('th-checkgroup', CheckGroup);
        Util.registerComponent('th-dialog', Dialog);
        Util.registerComponent('th-field', Field);
        Util.registerComponent('th-formula', Formula);
        Util.registerComponent('th-select', Select);
        Util.registerComponent('th-switch', Switch);
        Util.registerComponent('th-textbox', TextBox);
        Util.registerComponent('th-toast', Toast);
        Util.registerComponent('th-toptray', Toptray);
        Util.registerComponent('th-upload', Upload);
        Util.registerComponent('th-waterfall', Waterfall);
    }

    static alert(text, callback, isConfirm) {
        const dialog = Util.createElement(`<th-dialog>${text}</th-dialog>`);
        document.body.append(dialog);

        const buttons = [];
        if (isConfirm) {
            buttons.push({ label: Locale.get('NO') });
            buttons.push({ label: Locale.get('YES') });
        } else {
            buttons.push({ label: Locale.get('OK') });
        }

        Object.assign(buttons[buttons.length - 1], {
            primary: true,
            onclick: async (self) => {
                callback && await callback.call(self, self);
                self.hide();
            }
        })
        dialog.buttons = buttons;
        dialog.open(true);
    }

    static confirm(text, callback) {
        this.alert(text, callback, true);
    }

    static info(text, type, delay) {
        if (this.#toast) {
            this.#toast.remove();
            this.#toast = null;
        }
        this.#toast = Util.createElement(`<th-toast>${text}</th-toast>`);
        this.#toast.attr('type', type);
        this.#toast.attr('delay', delay);
        document.body.append(this.#toast);
    }

    static error(text) {
        this.info(text, 'error', 5000);
    }

    static warn(text) {
        this.info(text, 'warning', 4000);
    }

    static success(text) {
        this.info(text, 'success');
    }
}

window.Thyme = Thyme;
export default Thyme;