/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.reducer

import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TranslationsState
import mozilla.components.concept.engine.translate.TranslationOperation

internal object TranslationsStateReducer {

    @Suppress("LongMethod")
    fun reduce(state: BrowserState, action: TranslationsAction): BrowserState = when (action) {
        is TranslationsAction.TranslateExpectedAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    isExpectedTranslate = true,
                )
            }
        }

        is TranslationsAction.TranslateOfferAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    isOfferTranslate = true,
                )
            }
        }

        is TranslationsAction.TranslateStateChangeAction -> {
            if (action.translationEngineState.requestedTranslationPair != null) {
                state.copyWithTranslationsState(action.tabId) {
                    it.copy(
                        isTranslated = true,
                    )
                }
            }
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    translationEngineState = action.translationEngineState,
                )
            }
        }

        is TranslationsAction.TranslateAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(isTranslateProcessing = true)
            }

        is TranslationsAction.TranslateRestoreAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(isRestoreProcessing = true)
            }

        is TranslationsAction.TranslateSuccessAction -> {
            when (action.operation) {
                TranslationOperation.TRANSLATE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslated = true,
                            isTranslateProcessing = false,
                            translationError = null,
                        )
                    }
                }

                TranslationOperation.RESTORE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslated = false,
                            isRestoreProcessing = false,
                            translationError = null,
                        )
                    }
                }

                TranslationOperation.FETCH_LANGUAGES -> {
                    // Generally expect [TranslationsAction.TranslateSetLanguagesAction] to be used
                    // as the success condition since the updated value is needed.
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            translationError = null,
                        )
                    }
                }
            }
        }

        is TranslationsAction.TranslateExceptionAction -> {
            when (action.operation) {
                TranslationOperation.TRANSLATE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslateProcessing = false,
                            translationError = action.translationError,
                        )
                    }
                }

                TranslationOperation.RESTORE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isRestoreProcessing = false,
                            translationError = action.translationError,
                        )
                    }
                }

                TranslationOperation.FETCH_LANGUAGES -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            supportedLanguages = null,
                            translationError = action.translationError,
                        )
                    }
                }
            }
        }

        is TranslationsAction.TranslateSetLanguagesAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    supportedLanguages = action.supportedLanguages,
                    translationError = null,
                )
            }
    }

    private inline fun BrowserState.copyWithTranslationsState(
        tabId: String,
        crossinline update: (TranslationsState) -> TranslationsState,
    ): BrowserState {
        return updateTabOrCustomTabState(tabId) { current ->
            current.createCopy(translationsState = update(current.translationsState))
        }
    }
}
