


%----------------%
% Identification %
%----------------%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{SizheArticle}[01/09/18 Template for university coursework reports]
\LoadClass{article}  


%------------------%
% Default packages %
%------------------%
% amsmath : Math symbols
\RequirePackage{amsmath}

% amssymb : Extended math symbols such as \Box and \Diamond
\RequirePackage{amssymb}

% color : Defining colours
\RequirePackage{color}

% enumitem : Customise itemize/enumerate and reduce space with \begin{itemize}[nolistsep]
\RequirePackage{enumitem}

% float : For floating figures mid-text with [H]
\RequirePackage{float}

% graphicx : \includegraphics
\RequirePackage{graphicx}

% parskip : No indent and add line skip to paragraphs
\RequirePackage{parskip}

% subcaption : Subfigures
\RequirePackage{subcaption}

% vmargin : Wider margins than LaTeX default
\RequirePackage{vmargin}



%---------------------------------------%
% Useful packages (Uncomment if needed) %
%---------------------------------------%
% sizhecode.sty : Styles for code listings
%\RequirePackage{sizhecode}

% sizheplots.sty : Styles for plotting graphs
%\RequirePackage{sizheplots}

% syntax : Writing grammars such as in BNF form
%\RequirePackage{syntax}

% tikz : Drawing diagrams and plots (included in plots.sty)
%\RequirePackage{tikz}


%---------------------%
% Bibliography styles %
%---------------------%
\RequirePackage[%
    backend=biber,
    giveninits=true,
    doi=false,
    sortcites=true]{biblatex}
\setlength\bibitemsep{0.5\baselineskip}


%-----------------%
% Appendix styles %
%-----------------%
\RequirePackage[toc,page,title]{appendix}
%\RequirePackage{chngcntr}


%--------%
% Macros %
%--------%
% New line (deprecated by parskip)
\newcommand{\n}[0]{\\[\baselineskip]}


% Simple image in a figure \imagefig{Width}{Path}{Caption}
% #1 - Width of image
% #2 - Path to image
% #3 - Caption
\newcommand{\imagefig}[3]{
\begin{figure}[H]
	\centering
	\includegraphics[width=#1, keepaspectratio]{#2}
	\caption{#3}
\end{figure}
}



%----------------%
% Custom options %
%----------------%
% Table of contents max depth
\setcounter{tocdepth}{2}



