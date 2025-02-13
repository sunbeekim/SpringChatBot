import { CrawlRequest, CrawlStop, TxtDownload } from '../../api/testAPI';
import Loding from '../../utils/LoadingSVG';
import { useState } from 'react';

const Crwaling = () => {
  const [inputValue, setInputValue] = useState(''); // 입력값 상태
  const [inputUrl, setInputUrl] = useState('');

  const [inputDeep, setInputDeep] = useState('');
  const [result, setResult] = useState(null); // 결과값 상태
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [taskId, setTaskId] = useState(null); // 현재 작업 ID 상태
  const [errorMessage, setErrorMessage] = useState('');

  const { state } = { state: { user: { roleId: 1, username: 'subadmin' } } };
  const isAuthenticated = true;

  // 입력값 변경 핸들러
  const handleInputChange = (event) => setInputValue(event.target.value);
  const handleUrlChange = (event) => setInputUrl(event.target.value);
  const handleDeepChange = (event) => setInputDeep(event.target.value);

  // 작업 중지 핸들러
  const stopTask = async () => {
    if (!taskId) {
      setErrorMessage('중지할 작업이 없습니다.');
      return;
    }
    console.log('현재 taskId:', taskId);
    try {
      setLoading(true);
      const response = await CrawlStop(taskId);
      console.log('CrawlStop 요청 성공:', response.data);
      console.log('중지 결과:', response.data);
      setErrorMessage(`작업이 중지되었습니다: ${taskId}`);

      setTaskId(null);
    } catch (error) {
      console.error('작업 중지 중 오류:', error);
      setErrorMessage('작업 중지 요청 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const renderTree = (node, parentPath = '') => {
    if (!node) return null;

    const currentPath = `${parentPath}/${node.name}`;

    if (node.type === 'file') {
      return (
        <li key={currentPath} className="file" onClick={() => handleFileClick(currentPath)}>
          {node.name}
        </li>
      );
    }

    if (node.type === 'folder') {
      return (
        <li key={currentPath} className="folder">
          <details>
            <summary>{node.name}</summary>
            <ul>{node.children?.map((child) => renderTree(child, currentPath)) || []}</ul>
          </details>
        </li>
      );
    }

    return null;
  };

  const handleFileClick = (filePath) => {
    TxtDownload(filePath);
    console.log('파일 다운로드 클릭');
  };

  // 알고리즘 실행 핸들러
  const handlerCrawl = async () => {
    // if (!isAuthenticated) {
    //   setErrorMessage('사용자가 로그인되지 않았습니다.');
    //   return;
    // }
    if (inputValue.trim() === '') {
      setErrorMessage('단어를 입력해주세요.');
      return;
    }
    if (!/^https?:\/\/.+$/.test(inputUrl)) {
      setErrorMessage('올바른 URL을 입력해주세요.');
      return;
    }
    if (inputDeep < 1 || inputDeep > 5) {
      setErrorMessage('깊이는 1에서 5 사이의 숫자를 입력해주세요.');
      return;
    }

    setErrorMessage('');
    setLoading(true);
    try {
      const taskId = `task-${Date.now()}`;
      setTaskId(taskId);
      const response = await CrawlRequest(inputUrl, inputValue, inputDeep, state, taskId);
      const result = response.data;

      const summary = result[result.length - 1]?.summary;
      const data = result.slice(0, -1);
      const fileTree = result.find((item) => item.fileTree)?.fileTree; // fileTree 추출

      // console.log('API 응답 결과 (JSON):', JSON.stringify(result, null, 2)); // 보기 좋은 JSON 형태로 출력
      console.log('count:', summary);
      // console.log('크롤링 데이터:', data);
      // console.log('fileTree 데이터:', JSON.stringify(fileTree, null, 2));

      setResult({ data, summary, fileTree }); // 데이터 통합
    } catch (error) {
      setErrorMessage('API 요청 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
      setTaskId(null);
    }
  };

  return (
    <div>
      <div>https://ko.wikipedia.org/wiki</div>
      <div>현재 위키피디아만 구현</div>
      {/* <div>Open AI로 스크립트 작성해서 동적으로 구현했으나,</div>

      <div>스크립트 코드가 일관성이 없어서 종종 실패함</div>
      <div>크롤링의 퀄리티도 일정하지 않음</div>
      <div>사용자가 학습 할 주제와 연관주제에 대한 자료를 자동 스크롤링</div>
      <div>보기 편하게 이 페이지 내에서 프레그먼트? 카드형식? 등으로 출력</div>
      <div>학습 자료를 다운로드 가능하게</div>
      <div>크롤링한 데이터를 파인튜닝해서 사용자마다 개인AI를 가질 수 있게 생성 로직 짜야함</div>
      <div>AI를 이용해 학습자료를 바탕으로 난이도별 문제 작성</div> */}

      <div>
        <div>
          <label>주소</label>
          <input
            type="text"
            value={inputUrl}
            onChange={handleUrlChange}
            placeholder="주소 입력"
            disabled={loading}
          />
        </div>

        <div>
          <label>단어</label>
          <input
            type="text"
            value={inputValue}
            onChange={handleInputChange}
            placeholder="단어 입력"
            disabled={loading}
          />
        </div>

        <div>
          <label>깊이</label>
          <input
            type="text"
            value={inputDeep}
            onChange={handleDeepChange}
            placeholder="1~5"
            disabled={loading}
          />
        </div>

        <button
          onClick={handlerCrawl}
          disabled={!isAuthenticated || loading || state.user?.roleId !== 1}
        >
          {loading ? '실행 중' : '실행'} {/* 로딩 상태 표시 */}
        </button>
        <button onClick={stopTask} disabled={!taskId}>
          작업 중지
        </button>

        {!isAuthenticated && <p style={{ color: 'red' }}>로그인 후 실행 가능합니다.</p>}
      </div>
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
      {loading && <Loding />}
      <div>
        {/* 파일 트리 렌더링 */}
        {result?.fileTree ? (
          <div>
            <h3>파일 트리</h3>
            <ul className="file-tree">{renderTree(result.fileTree)}</ul>
          </div>
        ) : (
          <p>파일 트리가 없습니다.</p>
        )}
      </div>
      <div>
        {result ? (
          <div>
            {result.summary && (
              <div>
                <p>{result.summary}</p>
              </div>
            )}

            {/* 크롤링 결과 반복 출력 */}
            {result.data.map((item, index) => (
              <div key={index}>
                <p>==============================================</p>
                <p>단어: {item.word}</p>
                <p>퀄리티: {item.quality ? 'Good' : 'Bad'}</p>
                <p>
                  연관단어:{' '}
                  {item.relatedTopics && item.relatedTopics.length > 0
                    ? item.relatedTopics.join(', ')
                    : '값이 없습니다'}
                </p>
                <p>==============================================</p>
                <p>crawling 결과: {item.content}</p>
              </div>
            ))}
          </div>
        ) : (
          <p>결과값이 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default Crwaling;
