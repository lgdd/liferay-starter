port module Main exposing (..)

import Array
import Browser
import Dict exposing (Dict)
import Html exposing (Html, a, button, code, div, footer, h1, h2, header, i, input, label, li, node, option, p, pre, select, span, text, ul)
import Html.Attributes exposing (attribute, class, for, href, id, target, title, type_, value)
import Html.Events exposing (onClick, onInput)
import Json.Encode as JsonEncode
import Regex



---- MODEL ----


type alias Model =
    { apiHost : String
    , system : System
    , count : Int
    , workspace : LiferayWorkspace
    }


type alias LiferayWorkspace =
    { tool : String
    , wrapper : String
    , liferayVersion : String
    , projectGroupId : String
    , projectArtifactId : String
    , projectVersion : String
    , apps : Dict Int LiferayApp
    }


type alias LiferayApp =
    { id : Int
    , name : String
    , template : Maybe String
    , appType : LiferayAppType
    }


type LiferayAppType
    = Java
    | JavaScript
    | Theme


type System
    = Unix
    | Windows


tools : List String
tools =
    [ "Gradle", "Maven" ]


versions : List String
versions =
    [ "portal-7.4-ga2"
    , "dxp-7.3-sp1"
    , "dxp-7.2-sp4"
    , "dxp-7.1-sp5"
    , "dxp-7.0-sp15"
    , "portal-7.3-ga7"
    , "portal-7.2-ga2"
    , "portal-7.1-ga4"
    , "portal-7.0-ga7"
    , "commerce-2.0.7-7.2"
    , "commerce-2.0.7-7.1"
    ]


javaTemplates : List String
javaTemplates =
    [ "mvc-portlet", "service-builder", "rest-builder", "panel-app", "rest", "api" ]


javaScriptTemplates : List String
javaScriptTemplates =
    [ "react", "vuejs", "angular", "vanilla" ]


themeTemplates : List String
themeTemplates =
    [ "classic", "unstyled" ]


type alias Flags =
    { apiHost : String
    , platform : String
    }


init : Flags -> ( Model, Cmd Msg )
init flags =
    ( { apiHost = flags.apiHost
      , system = getSystemFromPlatform flags.platform
      , count = 0
      , workspace =
            { tool = getDefaultTool
            , wrapper = getToolWrapper getDefaultTool
            , liferayVersion = getDefaultVersion
            , projectGroupId = "org.acme"
            , projectArtifactId = "liferay-project"
            , projectVersion = "1.0.0-SNAPSHOT"
            , apps = Dict.fromList []
            }
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
    | AddApp LiferayAppType
    | UpdateAppName LiferayApp String
    | UpdateAppTemplate LiferayApp String
    | RemoveApp LiferayApp


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateTool newTool ->
            let
                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | tool = newTool, wrapper = getToolWrapper newTool }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateLiferayVersion newLiferayVersion ->
            let
                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | liferayVersion = newLiferayVersion }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateProjectGroupId newProjectGroupId ->
            let
                newGroupId =
                    if String.isEmpty newProjectGroupId then
                        "org.acme"

                    else
                        newProjectGroupId

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | projectGroupId = newGroupId }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateProjectArtifactId newArtifactId ->
            let
                artifactId =
                    toKebabCase newArtifactId "liferay-project"

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | projectArtifactId = artifactId }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateProjectVersion newProjectVersion ->
            let
                newVersion =
                    if String.isEmpty newProjectVersion then
                        "1.0.0-SNAPSHOT"

                    else
                        newProjectVersion

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | projectVersion = newVersion }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateSystem newSystem ->
            ( { model | system = newSystem }, Cmd.none )

        AddApp appType ->
            let
                newId =
                    model.count + 1

                newApp : LiferayApp
                newApp =
                    case appType of
                        Java ->
                            { id = newId
                            , name = getDefaultAppName Java getDefaultJavaTemplate
                            , template = getDefaultJavaTemplate
                            , appType = Java
                            }

                        JavaScript ->
                            { id = newId
                            , name = getDefaultAppName JavaScript getDefaultJavaScriptTemplate
                            , template = getDefaultJavaScriptTemplate
                            , appType = JavaScript
                            }

                        Theme ->
                            { id = newId
                            , name = getDefaultAppName Theme Nothing
                            , template = Just "classic"
                            , appType = Theme
                            }

                shouldUpdateAppName =
                    not
                        (Dict.values model.workspace.apps
                            |> List.filter (\app -> app.name == newApp.name)
                            |> List.isEmpty
                        )

                newAppName =
                    if shouldUpdateAppName then
                        newApp.name ++ "-" ++ toLetters (newId - 1) ""

                    else
                        newApp.name

                updatedNewApp =
                    { newApp | name = newAppName }

                newApps =
                    Dict.insert newId updatedNewApp model.workspace.apps

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | apps = newApps }
            in
            ( { model | workspace = newWorkspace, count = newId }, Cmd.none )

        UpdateAppName app newName ->
            let
                newFormattedName =
                    toKebabCase newName app.name

                newApp =
                    { app | name = newFormattedName }

                newApps =
                    Dict.update app.id (Maybe.map (\_ -> newApp)) model.workspace.apps

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | apps = newApps }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        UpdateAppTemplate app newTemplate ->
            let
                newName =
                    if
                        app.name
                            == getDefaultAppName app.appType (Just newTemplate)
                            || app.name
                            == (getDefaultAppName app.appType (Just newTemplate) ++ "-" ++ toLetters (app.id - 1) "")
                    then
                        if
                            not
                                (Dict.values model.workspace.apps
                                    |> List.filter (\a -> a.name == getDefaultAppName app.appType (Just newTemplate))
                                    |> List.isEmpty
                                )
                        then
                            getDefaultAppName app.appType (Just newTemplate) ++ "-" ++ toLetters (app.id - 1) ""

                        else
                            getDefaultAppName app.appType (Just newTemplate)

                    else
                        app.name

                newApp =
                    { app | template = Just newTemplate, name = newName }

                newApps =
                    Dict.update app.id (Maybe.map (\_ -> newApp)) model.workspace.apps

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | apps = newApps }
            in
            ( { model | workspace = newWorkspace }, Cmd.none )

        RemoveApp toRemoveApp ->
            let
                newApps =
                    Dict.remove toRemoveApp.id model.workspace.apps

                workspace =
                    model.workspace

                newWorkspace =
                    { workspace | apps = newApps }

                newCount =
                    model.count - 1
            in
            ( { model | workspace = newWorkspace, count = newCount }, Cmd.none )

        DownloadWorkspace ->
            ( model, downloadWorkspace (getDownloadWorkspaceData model) )

        ToggleDark _ ->
            ( model, toggleDark () )

        CopyToClipboard containerId ->
            ( model, copyToClipboard containerId )



---- VIEW ----


view : Model -> Html Msg
view model =
    div []
        [ viewHeader
        , node "main"
            [ attribute "role" "main" ]
            [ div [ class "container-fluid container-fluid-max-xl" ]
                [ div [ class "row" ]
                    [ viewWorkspaceConfig model
                    , viewAppsConfig model
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
    div []
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
    header [ class "container-fluid container-fluid-max-xl container-form-lg" ]
        [ div [ class "row" ]
            [ div [ class "col-3" ] [ h1 [] [ text "Liferay Starter" ] ]
            , div [ class "col-1" ] [ viewThemeSwitch ]
            , div [ class "col" ] [ viewGithubButtons ]
            ]
        , div [ class "row" ]
            [ div [ class "col" ]
                [ i [ class "d-none d-md-block" ]
                    [ p [] [ text "No plugin or tool required." ]
                    , p [] [ text "Choose your favorite IDE and get ready to code." ]
                    ]
                ]
            ]
        ]


viewWorkspaceConfig : Model -> Html Msg
viewWorkspaceConfig model =
    div [ class "col" ]
        [ div [ class "row" ]
            [ div [ class "col-md" ] [ h2 [ class "mb-4" ] [ text "Workspace" ] ] ]
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


viewAppsConfig : Model -> Html Msg
viewAppsConfig model =
    div [ class "col ml-5" ]
        [ div [ class "row" ]
            [ div [ class "col-md" ] [ h2 [ class "mb-4" ] [ text "Apps" ] ] ]
        , div [ id "apps-buttons", class "row" ]
            [ div [ class "col" ]
                [ button
                    [ class "btn btn-secondary"
                    , onClick (AddApp Java)
                    ]
                    [ text "Add Java Module"
                    ]
                ]
            , div [ class "col" ]
                [ button
                    [ class "btn btn-secondary"
                    , onClick (AddApp JavaScript)
                    ]
                    [ text "Add JS Module"
                    ]
                ]
            , div [ class "col" ]
                [ button
                    [ class "btn btn-secondary"
                    , onClick (AddApp Theme)
                    ]
                    [ text "Add Theme"
                    ]
                ]
            ]
        , viewApps model
        ]


viewApps : Model -> Html Msg
viewApps model =
    div [] (Dict.values (Dict.map (viewApp model) model.workspace.apps))


viewApp : Model -> Int -> LiferayApp -> Html Msg
viewApp model id_ app =
    let
        hasError =
            appNameAlreadyUsed app.name model

        formGroupClassName =
            if hasError then
                "form-group has-error"

            else
                "form-group"

        feedbackError =
            if hasError then
                div [ class "form-feedback-group" ]
                    [ div [ class "form-feedback-item" ]
                        [ text "This name is already used."
                        ]
                    ]

            else
                text ""

        templates =
            case app.appType of
                Java ->
                    div [ class "col" ] [ viewJavaTemplates app ]

                JavaScript ->
                    div [ class "col" ] [ viewJavaScriptTemplates app ]

                Theme ->
                    if
                        String.contains "7.0" model.workspace.liferayVersion
                            || String.contains "7.1" model.workspace.liferayVersion
                    then
                        text ""

                    else
                        div [ class "col" ] [ viewThemeTemplates app ]
    in
    div [ class "row" ]
        [ div [ class "col" ]
            [ div [ class formGroupClassName ]
                [ input
                    [ id ("app-" ++ String.fromInt id_)
                    , class "form-control"
                    , type_ "text"
                    , value app.name
                    , onInput (UpdateAppName app)
                    ]
                    []
                , feedbackError
                ]
            ]
        , templates
        , div [ class "col" ]
            [ button
                [ class "btn btn-danger"
                , onClick (RemoveApp app)
                ]
                [ text "Remove"
                ]
            ]
        ]


viewJavaTemplates : LiferayApp -> Html Msg
viewJavaTemplates app =
    div [ class "form-group" ]
        [ select [ id "selectJavaTemplate", class "form-control", onInput (UpdateAppTemplate app) ]
            (List.map viewOption javaTemplates)
        ]


viewJavaScriptTemplates : LiferayApp -> Html Msg
viewJavaScriptTemplates app =
    div [ class "form-group" ]
        [ select [ id "selectJavaScriptTemplate", class "form-control", onInput (UpdateAppTemplate app) ]
            (List.map viewOption javaScriptTemplates)
        ]


viewThemeTemplates : LiferayApp -> Html Msg
viewThemeTemplates app =
    div [ class "form-group" ]
        [ select [ id "selectThemeTemplate", class "form-control", onInput (UpdateAppTemplate app) ]
            (List.map viewOption themeTemplates)
        ]


viewGithubButtons : Html Msg
viewGithubButtons =
    span [ class "gh-btn-list" ]
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
        [ label [ for "selectLiferayVersion" ] [ text "Liferay Product Version" ]
        , select [ id "selectLiferayVersion", class "form-control", onInput UpdateLiferayVersion ]
            (List.map viewOption versions)
        ]


viewOption : String -> Html Msg
viewOption tool =
    option [ value (String.toLower tool) ] [ text tool ]


viewInputGroupId : Model -> Html Msg
viewInputGroupId model =
    let
        hasError =
            not (Regex.contains javaPackagePattern model.workspace.projectGroupId)

        formGroupClassName =
            if hasError then
                "form-group has-error"

            else
                "form-group"

        feedbackError =
            if hasError then
                div [ class "form-feedback-group" ]
                    [ div [ class "form-feedback-item" ]
                        [ text "Please enter a valid package name."
                        ]
                    ]

            else
                text ""
    in
    div [ class formGroupClassName ]
        [ label [ for "groupId" ] [ text "Project Group ID" ]
        , input
            [ id "groupId"
            , class "form-control"
            , type_ "text"
            , value model.workspace.projectGroupId
            , onInput UpdateProjectGroupId
            ]
            []
        , feedbackError
        ]


viewInputArtifactId : Model -> Html Msg
viewInputArtifactId model =
    div [ class "form-group" ]
        [ label [ for "artifactId" ] [ text "Project Artifact ID" ]
        , input
            [ id "artifactId"
            , class "form-control"
            , type_ "text"
            , value model.workspace.projectArtifactId
            , onInput UpdateProjectArtifactId
            ]
            []
        ]


viewInputProjectVersion : Model -> Html Msg
viewInputProjectVersion model =
    let
        hasError =
            not (Regex.contains semverPattern model.workspace.projectVersion)

        formGroupClassName =
            if hasError then
                "form-group has-error"

            else
                "form-group"

        feedbackError =
            if hasError then
                div [ class "form-feedback-group" ]
                    [ div [ class "form-feedback-item" ]
                        [ text "Please enter a valid version."
                        ]
                    ]

            else
                text ""
    in
    div [ class formGroupClassName ]
        [ label [ for "projectVersion" ] [ text "Project Version" ]
        , input
            [ id "projectVersion"
            , class "form-control"
            , type_ "text"
            , value model.workspace.projectVersion
            , onInput UpdateProjectVersion
            ]
            []
        , feedbackError
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


getDownloadWorkspaceData : Model -> JsonEncode.Value
getDownloadWorkspaceData model =
    let
        url =
            getDownloadWorkspaceUrl model
    in
    JsonEncode.object
        [ ( "url", JsonEncode.string url )
        , ( "workspace"
          , JsonEncode.object
                [ ( "projectGroupId", JsonEncode.string model.workspace.projectGroupId )
                , ( "projectArtifactId", JsonEncode.string model.workspace.projectArtifactId )
                , ( "projectVersion", JsonEncode.string model.workspace.projectVersion )
                , ( "apps", formatAppsToJson model.workspace.apps )
                ]
          )
        ]


formatAppsToJson : Dict Int LiferayApp -> JsonEncode.Value
formatAppsToJson apps =
    JsonEncode.list formatAppToJson (Dict.toList apps)


formatAppToJson : ( Int, LiferayApp ) -> JsonEncode.Value
formatAppToJson ( id, app ) =
    JsonEncode.object
        [ ( "id", JsonEncode.int id )
        , ( "name", JsonEncode.string app.name )
        , ( "type", JsonEncode.string (formatAppType app.appType) )
        , ( "template", JsonEncode.string (formatAppTemplate (Maybe.withDefault "theme" app.template)) )
        ]


formatAppTemplate : String -> String
formatAppTemplate template =
    String.toUpper template
        |> userReplace "-" (\_ -> "_")


formatAppType : LiferayAppType -> String
formatAppType appType =
    case appType of
        Java ->
            "JAVA"

        JavaScript ->
            "JAVASCRIPT"

        Theme ->
            "THEME"


userReplace : String -> (Regex.Match -> String) -> String -> String
userReplace userRegex replacer string =
    case Regex.fromString userRegex of
        Nothing ->
            string

        Just regex ->
            Regex.replace regex replacer string


appNameAlreadyUsed : String -> Model -> Bool
appNameAlreadyUsed appName model =
    (Dict.values model.workspace.apps
        |> List.filter (\app -> app.name == appName)
        |> List.length
    )
        > 1


getDefaultJavaTemplate : Maybe String
getDefaultJavaTemplate =
    Array.get 0 (Array.fromList javaTemplates)


getDefaultJavaScriptTemplate : Maybe String
getDefaultJavaScriptTemplate =
    Array.get 0 (Array.fromList javaScriptTemplates)


getDefaultAppName : LiferayAppType -> Maybe String -> String
getDefaultAppName appType template =
    let
        templateName =
            case template of
                Just name ->
                    "my-" ++ name

                Nothing ->
                    "my-app"
    in
    case appType of
        Java ->
            templateName

        JavaScript ->
            templateName

        Theme ->
            "my-theme"


getDefaultVersion : String
getDefaultVersion =
    Maybe.withDefault "gradle" (Array.get 0 (Array.fromList versions))


getDefaultTool : String
getDefaultTool =
    String.toLower (Maybe.withDefault "gradle" (Array.get 0 (Array.fromList tools)))


getToolWrapper : String -> String
getToolWrapper tool =
    if tool == "gradle" then
        "gradlew"

    else
        "mvnw"


getZipFileName : Model -> String
getZipFileName model =
    if model.workspace.projectArtifactId == "" then
        model.workspace.tool ++ "-liferay-workspace-" ++ model.workspace.liferayVersion ++ ".zip"

    else
        model.workspace.projectArtifactId ++ ".zip"


getDownloadWorkspaceUrl : Model -> String
getDownloadWorkspaceUrl model =
    model.apiHost
        ++ "/api/liferay/"
        ++ model.workspace.liferayVersion
        ++ "/workspace/"
        ++ model.workspace.tool


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
        wrapper =
            if model.workspace.tool == "gradle" then
                "./gradlew"

            else
                "./mvnw"
    in
    "mkdir "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "cp "
        ++ getZipFileName model
        ++ " "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "cd "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "jar xvf "
        ++ getZipFileName model
        ++ " && "
        ++ "rm "
        ++ getZipFileName model
        ++ " && chmod +x "
        ++ model.workspace.wrapper
        ++ " && "
        ++ getBuildDeployCmd model wrapper


getInitCmdWindows : Model -> String
getInitCmdWindows model =
    let
        wrapper =
            if model.workspace.tool == "gradle" then
                "gradlew.bat"

            else
                "mvnw.cmd"
    in
    "mkdir "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "copy "
        ++ getZipFileName model
        ++ " "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "cd "
        ++ model.workspace.projectArtifactId
        ++ " && "
        ++ "jar xvf "
        ++ getZipFileName model
        ++ " && "
        ++ "del "
        ++ getZipFileName model
        ++ " && "
        ++ getBuildDeployCmd model wrapper


getBuildDeployCmd : Model -> String -> String
getBuildDeployCmd model wrapper =
    let
        serviceBuilders =
            getServiceBuilders model

        initCmd =
            if model.workspace.tool == "gradle" then
                "initBundle"

            else
                "bundle-support:init"

        buildCmd =
            if model.workspace.tool == "gradle" then
                "build"

            else
                "package"

        deployCmd =
            if model.workspace.tool == "gradle" then
                "deploy"

            else
                "bundle-support:deploy"

        serviceBuilderCmd =
            if model.workspace.tool == "gradle" then
                getServiceBuilderGradleGoals serviceBuilders

            else
                getServiceBuilderMavenGoal serviceBuilders
    in
    if Dict.isEmpty model.workspace.apps then
        String.join " " [ wrapper, initCmd ]

    else if not (List.isEmpty serviceBuilders) then
        if model.workspace.tool == "gradle" then
            String.join " " [ wrapper, serviceBuilderCmd, initCmd, buildCmd, deployCmd ]

        else
            String.join " " [ wrapper, serviceBuilderCmd, "&&", wrapper, initCmd, buildCmd, deployCmd ]

    else
        String.join " " [ wrapper, initCmd, buildCmd, deployCmd ]


getServiceBuilderMavenGoal : List LiferayApp -> String
getServiceBuilderMavenGoal apps =
    let
        serviceBuilderPaths =
            List.map getServiceBuilderRelativePath apps
                |> String.join " "
    in
    "service-builder:build --projects " ++ serviceBuilderPaths


getServiceBuilderGradleGoals : List LiferayApp -> String
getServiceBuilderGradleGoals apps =
    List.map getServiceBuilderGradleGoal apps
        |> String.join " "


getServiceBuilderGradleGoal : LiferayApp -> String
getServiceBuilderGradleGoal app =
    ":modules:" ++ app.name ++ ":" ++ app.name ++ "-service:buildService"


getServiceBuilderRelativePath : LiferayApp -> String
getServiceBuilderRelativePath app =
    "modules/" ++ app.name ++ "/" ++ app.name ++ "-service"


getServiceBuilders : Model -> List LiferayApp
getServiceBuilders model =
    Dict.values model.workspace.apps
        |> List.filter (\app -> app.template == Just "service-builder")


getSystemFromPlatform : String -> System
getSystemFromPlatform platform =
    let
        platformLowerCase =
            String.toLower platform
    in
    if String.contains "win" platformLowerCase then
        Windows

    else
        Unix


toLetters : Int -> String -> String
toLetters number letters =
    let
        modulo =
            modBy 26 (number - 1)

        char =
            Char.fromCode (65 + modulo)

        newLetters =
            String.concat
                [ String.toLower (String.fromChar char)
                , letters
                ]

        dividend =
            (number - modulo) // 26
    in
    if dividend > 0 then
        toLetters dividend newLetters

    else
        newLetters


javaPackagePattern : Regex.Regex
javaPackagePattern =
    Regex.fromString "^(?:\\w+|\\w+\\.\\w+)+$"
        |> Maybe.withDefault Regex.never


semverPattern : Regex.Regex
semverPattern =
    Regex.fromString "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
        |> Maybe.withDefault Regex.never


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


port downloadWorkspace : JsonEncode.Value -> Cmd msg



---- PROGRAM ----


main : Program Flags Model Msg
main =
    Browser.element
        { view = view
        , init = init
        , update = update
        , subscriptions = always Sub.none
        }
