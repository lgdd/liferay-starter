port module Main exposing (..)

import Browser
import Html exposing (Html, a, button, code, div, footer, h1, h2, i, input, label, li, node, option, p, pre, select, span, text, ul)
import Html.Attributes exposing (attribute, class, for, href, id, target, title, type_, value)
import Html.Events exposing (onClick, onInput)
import Regex



---- MODEL ----


type alias Model =
    { apiHost : String
    , system : System
    , tool : String
    , liferayVersion : String
    , toolWrapper : String
    , projectGroupId : String
    , projectArtifactId : String
    , projectVersion : String
    }


type System
    = Unix
    | Windows


tools : List String
tools =
    [ "Gradle", "Maven" ]


versions : List String
versions =
    [ "7.3", "7.2", "7.1", "7.0" ]


type alias Flags =
    { apiHost : String }


init : Flags -> ( Model, Cmd Msg )
init flags =
    ( { apiHost = flags.apiHost
      , system = Unix
      , tool = "gradle"
      , liferayVersion = "7.3"
      , toolWrapper = "gradlew"
      , projectGroupId = "org.acme"
      , projectArtifactId = "liferay-project"
      , projectVersion = "1.0.0-SNAPSHOT"
      }
    , initTheme ()
    )



---- UPDATE ----


type Msg
    = UpdateTool String
    | UpdateLiferayVersion String
    | UpdateProjectGroupId String
    | UpdateProjectArtifactId String
    | UpdateProjectVersion String
    | UpdateSystem System
    | DownloadWorkspace
    | ToggleDark String
    | CopyToClipboard String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateTool newTool ->
            ( { model
                | tool = newTool
                , toolWrapper = getToolWrapper newTool
              }
            , Cmd.none
            )

        UpdateLiferayVersion newLiferayVersion ->
            ( { model | liferayVersion = newLiferayVersion }, Cmd.none )

        UpdateProjectGroupId newProjectGroupId ->
            ( { model | projectGroupId = newProjectGroupId }, Cmd.none )

        UpdateProjectArtifactId newArtifactId ->
            let
                artifactId =
                    toKebabCase newArtifactId "liferay-project"
            in
            ( { model | projectArtifactId = artifactId }, Cmd.none )

        UpdateProjectVersion newProjectVersion ->
            ( { model | projectVersion = newProjectVersion }, Cmd.none )

        UpdateSystem newSystem ->
            ( { model | system = newSystem }, Cmd.none )

        DownloadWorkspace ->
            ( model, downloadWorkspace (getDownloadWorkspaceUrl model) )

        ToggleDark _ ->
            ( model, toggleDark () )

        CopyToClipboard containerId ->
            ( model, copyToClipboard containerId )



---- VIEW ----


view : Model -> Html Msg
view model =
    div []
        [ node "main"
            [ attribute "role" "main" ]
            [ div [ class "container-fluid container-fluid-max-lg container-form-lg" ]
                [ viewThemeSwitch
                , viewHeader
                , viewGithubButtons
                , div [ class "row" ]
                    [ div [ class "col-md" ] [ h2 [ class "mb-4" ] [ text "Configure" ] ] ]
                , div [ class "row" ]
                    [ div [ class "col-md" ] [ viewSelectTool ]
                    , div [ class "col-md" ] [ viewSelectLiferayVersion ]
                    ]
                , div [ class "row" ]
                    [ div [ class "col-md" ] [ viewInputGroupId model ]
                    , div [ class "col-md" ] [ viewInputArtifactId model ]
                    , div [ class "col-md" ] [ viewInputProjectVersion model ]
                    ]
                , div [ class "row" ]
                    [ div [ class "col-md" ]
                        [ div [ class "form-group" ]
                            [ button [ id "downloadWorkspace", class "btn btn-primary", onClick DownloadWorkspace ]
                                [ text "Generate your workspace"
                                ]
                            ]
                        ]
                    ]
                , div [ class "row" ]
                    [ div [ class "col-md" ] [ h2 [ class "mb-4" ] [ text "What now?" ] ] ]
                , div [ class "row" ]
                    [ div [ class "col-md" ]
                        [ p [] [ text "Unzip your workspace and intialize your Liferay bundle:" ]
                        , viewInitCmd model
                        ]
                    ]
                ]
            ]
        , footer [ class "footer mt-auto py-3" ]
            [ div [ class "container text-center" ]
                [ span [ class "text-muted" ]
                    [ text "MIT Licensed | Made with ❤ by "
                    , a [ href "https://github.com/lgdd", target "_blank" ] [ text "lgd" ]
                    ]
                ]
            ]
        ]


viewThemeSwitch : Html Msg
viewThemeSwitch =
    div [ class "theme-switch" ]
        [ label [ class "toggle-switch" ]
            [ input [ class "toggle-switch-check", type_ "checkbox", onInput ToggleDark ] []
            , span [ class "toggle-switch-bar" ]
                [ span
                    [ class "toggle-switch-handle"
                    , attribute "data-label-off" ""
                    , attribute "data-label-on" ""
                    ]
                    [ span [ class "button-icon button-icon-off toggle-switch-icon" ]
                        [ i [ class "fa fa-sun-o", attribute "aria-hidden" "true" ] []
                        ]
                    , span [ class "button-icon button-icon-on toggle-switch-icon" ]
                        [ i [ class "fa fa-moon-o", attribute "aria-hidden" "true" ] []
                        ]
                    ]
                ]
            ]
        ]


viewHeader : Html Msg
viewHeader =
    div [ class "row" ]
        [ div [ class "col-md" ]
            [ h1 [ class "mb-3 text-center" ] [ text "Liferay Starter" ] ]
        ]


viewGithubButtons : Html Msg
viewGithubButtons =
    div [ class "row" ]
        [ div [ class "col-md mb-4 text-center gh-btn-list" ]
            [ a
                [ class "github-button"
                , href "https://github.com/lgdd/liferay-starter"
                , attribute "data-icon" "octicon-star"
                , attribute "data-show-count" "true"
                , attribute "aria-label" "Star lgdd/liferay-starter on GitHub"
                ]
                [ text "Star" ]
            , a
                [ class "ml-4 github-button"
                , href "https://github.com/lgdd/liferay-starter/fork"
                , attribute "data-icon" "octicon-repo-forked"
                , attribute "data-show-count" "true"
                , attribute "aria-label" "Fork lgdd/liferay-starter on GitHub"
                ]
                [ text "Fork" ]
            ]
        ]


viewSelectTool : Html Msg
viewSelectTool =
    div [ class "form-group" ]
        [ label [ for "selectTool" ] [ text "Build Tool" ]
        , select [ id "selectTool", class "form-control", onInput UpdateTool ]
            (List.map viewOption tools)
        ]


viewSelectLiferayVersion : Html Msg
viewSelectLiferayVersion =
    div [ class "form-group" ]
        [ label [ for "selectLiferayVersion" ] [ text "Liferay Version" ]
        , select [ id "selectLiferayVersion", class "form-control", onInput UpdateLiferayVersion ]
            (List.map viewOption versions)
        ]


viewOption : String -> Html Msg
viewOption tool =
    option [ value (String.toLower tool) ] [ text tool ]


viewInputGroupId : Model -> Html Msg
viewInputGroupId model =
    div [ class "form-group" ]
        [ label [ for "groupId" ] [ text "Project Group ID" ]
        , input
            [ id "groupId"
            , class "form-control"
            , type_ "text"
            , value model.projectGroupId
            , onInput UpdateProjectGroupId
            ]
            []
        ]


viewInputArtifactId : Model -> Html Msg
viewInputArtifactId model =
    div [ class "form-group" ]
        [ label [ for "artifactId" ] [ text "Project Artifact ID" ]
        , input
            [ id "artifactId"
            , class "form-control"
            , type_ "text"
            , value model.projectArtifactId
            , onInput UpdateProjectArtifactId
            ]
            []
        ]


viewInputProjectVersion : Model -> Html Msg
viewInputProjectVersion model =
    div [ class "form-group" ]
        [ label [ for "projectVersion" ] [ text "Project Version" ]
        , input
            [ id "projectVersion"
            , class "form-control"
            , type_ "text"
            , value model.projectVersion
            , onInput UpdateProjectVersion
            ]
            []
        ]


viewInitCmd : Model -> Html Msg
viewInitCmd model =
    div [ id "code-container" ]
        [ p []
            [ viewInitCmdPanels model
            ]
        ]


viewInitCmdPanels : Model -> Html Msg
viewInitCmdPanels model =
    let
        unixClassNames =
            case model.system of
                Unix ->
                    ( "nav-link active", "active fade show tab-pane" )

                Windows ->
                    ( "nav-link", "fade tab-pane" )

        windowsClassNames =
            case model.system of
                Unix ->
                    ( "nav-link", "fade tab-pane" )

                Windows ->
                    ( "nav-link active", "active fade show tab-pane" )
    in
    pre []
        [ button
            [ id "copy-cmd"
            , class "btn btn-sm btn-default"
            , title "Copy to clipboard"
            , onClick (CopyToClipboard "#code-container .tab-pane.active .init-cmd")
            ]
            [ i [ class "fa fa-clipboard", attribute "aria-hidden" "true" ] [] ]
        , ul [ class "nav nav-underline mb-3", attribute "role" "tablist" ]
            [ li [ class "nav-item" ]
                [ span
                    [ id "unixTab"
                    , class (Tuple.first unixClassNames)
                    , attribute "data-toggle" "tab"
                    , attribute "role" "tab"
                    , attribute "aria-controls" "unix"
                    , attribute "aria-expanded" "true"
                    , onClick (UpdateSystem Unix)
                    ]
                    [ text "Linux / Mac" ]
                ]
            , li [ class "nav-item" ]
                [ span
                    [ id "windowsTab"
                    , class (Tuple.first windowsClassNames)
                    , attribute "data-toggle" "tab"
                    , attribute "role" "tab"
                    , attribute "aria-controls" "windows"
                    , attribute "aria-expanded" "true"
                    , onClick (UpdateSystem Windows)
                    ]
                    [ text "Windows" ]
                ]
            ]
        , div [ class "tab-content" ]
            [ div
                [ id "unix"
                , class (Tuple.second unixClassNames)
                , attribute "role" "tabpanel"
                , attribute "aria-labelledby" "unixTab"
                ]
                [ code [ class "init-cmd" ] [ text (getInitCmd model) ] ]
            , div
                [ id "windows"
                , class (Tuple.second windowsClassNames)
                , attribute "role" "tabpanel"
                , attribute "aria-labelledby" "windowsTab"
                ]
                [ code [ class "init-cmd" ] [ text (getInitCmd model) ] ]
            ]
        ]


getToolWrapper : String -> String
getToolWrapper tool =
    if tool == "gradle" then
        "gradlew"

    else
        "mvnw"


getZipFileName : Model -> String
getZipFileName model =
    if model.projectArtifactId == "" then
        model.tool ++ "-liferay-workspace-" ++ model.liferayVersion ++ ".zip"

    else
        model.projectArtifactId ++ ".zip"


getDownloadWorkspaceUrl : Model -> String
getDownloadWorkspaceUrl model =
    model.apiHost
        ++ "/api/workspace/"
        ++ model.tool
        ++ "/"
        ++ model.liferayVersion
        ++ "?projectGroupId="
        ++ model.projectGroupId
        ++ "&projectArtifactId="
        ++ model.projectArtifactId
        ++ "&projectVersion="
        ++ model.projectVersion


getInitCmd : Model -> String
getInitCmd model =
    case model.system of
        Unix ->
            getInitCmdUnix model

        Windows ->
            getInitCmdWindows model


getInitCmdUnix : Model -> String
getInitCmdUnix model =
    let
        initCmd =
            if model.tool == "gradle" then
                "./gradlew initBundle"

            else
                "./mvnw bundle-support:init"
    in
    "mkdir "
        ++ model.projectArtifactId
        ++ " && "
        ++ "cp "
        ++ getZipFileName model
        ++ " "
        ++ model.projectArtifactId
        ++ " && "
        ++ "cd "
        ++ model.projectArtifactId
        ++ " && "
        ++ "jar xvf "
        ++ getZipFileName model
        ++ " && "
        ++ "rm "
        ++ getZipFileName model
        ++ " && chmod +x "
        ++ model.toolWrapper
        ++ " && "
        ++ initCmd


getInitCmdWindows : Model -> String
getInitCmdWindows model =
    let
        initCmd =
            if model.tool == "gradle" then
                "gradlew.bat initBundle"

            else
                "mvnw.cmd bundle-support:init"
    in
    "mkdir "
        ++ model.projectArtifactId
        ++ " && "
        ++ "copy "
        ++ getZipFileName model
        ++ " "
        ++ model.projectArtifactId
        ++ " && "
        ++ "cd "
        ++ model.projectArtifactId
        ++ " && "
        ++ "jar xvf "
        ++ getZipFileName model
        ++ " && "
        ++ "del "
        ++ getZipFileName model
        ++ " && "
        ++ initCmd


toKebabCase : String -> String -> String
toKebabCase src defaultName =
    if String.isEmpty src then
        defaultName

    else
        let
            mr1 =
                Regex.fromString "[^a-zA-Z0-9]+"

            mr2 =
                Regex.fromString "[\\s\\.\\-]"

            mr3 =
                Regex.fromString "([a-z\\d])([A-Z])"

            mr4 =
                Regex.fromString "([A-Z]+)([A-Z][a-z\\d]+)"

            sep =
                "-"

            subsep r =
                case r.submatches of
                    fst :: snd :: _ ->
                        Maybe.map2 (\f s -> f ++ sep ++ s) fst snd
                            |> Maybe.withDefault r.match

                    _ ->
                        r.match
        in
        Maybe.map4
            (\r1 r2 r3 r4 ->
                src
                    |> Regex.split r1
                    |> List.map
                        (\word ->
                            word
                                |> Regex.replace r2 (\_ -> sep)
                                |> Regex.replace r3 subsep
                                |> Regex.replace r4 subsep
                        )
                    |> String.join sep
                    |> String.toLower
            )
            mr1
            mr2
            mr3
            mr4
            |> Maybe.withDefault defaultName



---- PORTS ----


port toggleDark : () -> Cmd msg


port initTheme : () -> Cmd msg


port copyToClipboard : String -> Cmd msg


port downloadWorkspace : String -> Cmd msg



---- PROGRAM ----


main : Program Flags Model Msg
main =
    Browser.element
        { view = view
        , init = init
        , update = update
        , subscriptions = always Sub.none
        }
